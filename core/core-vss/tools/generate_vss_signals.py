#!/usr/bin/env python3
"""Generate Kotlin VSS signal containers from docs/vss.csv."""

from __future__ import annotations

import csv
import re
from dataclasses import dataclass, field
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
SOURCE = ROOT / "docs" / "vss.csv"
OUTPUT = (
    ROOT
    / "core"
    / "core-vss"
    / "src"
    / "main"
    / "kotlin"
    / "com"
    / "monsters"
    / "mobimon"
    / "core"
    / "vss"
    / "generated"
)
PACKAGE = "com.monsters.mobimon.core.vss.generated"
KEYWORDS = {
    "as",
    "break",
    "class",
    "continue",
    "do",
    "else",
    "false",
    "for",
    "fun",
    "if",
    "in",
    "interface",
    "is",
    "null",
    "object",
    "package",
    "return",
    "super",
    "this",
    "throw",
    "true",
    "try",
    "typealias",
    "typeof",
    "val",
    "var",
    "when",
    "while",
}


@dataclass
class Node:
    name: str
    data_type: str = ""
    default: str = ""
    children: dict[str, "Node"] = field(default_factory=dict)

    @property
    def is_leaf(self) -> bool:
        return not self.children


def kotlin_type(data_type: str) -> str:
    normalized = data_type.lower()
    if normalized.endswith("[]"):
        return f"List<{kotlin_type(normalized[:-2])}>"
    if normalized in {"boolean", "bool"}:
        return "Boolean"
    if normalized in {"float", "double"}:
        return "Float"
    if normalized in {"string"}:
        return "String"
    if normalized in {"int8", "int16", "int32", "int64", "uint8", "uint16", "uint32", "uint64"}:
        return "Int"
    return "String"


def kotlin_default(data_type: str, default: str) -> str:
    target = kotlin_type(data_type)
    cleaned = default.strip()
    if target.startswith("List<"):
        return "emptyList()"
    if target == "Boolean":
        return "true" if cleaned.lower() == "true" else "false"
    if target == "Float":
        if not cleaned:
            return "0f"
        return f"{cleaned}f" if "." in cleaned else f"{cleaned}.0f"
    if target == "Int":
        return cleaned if re.fullmatch(r"-?\d+", cleaned) else "0"
    escaped = cleaned.replace("\\", "\\\\").replace('"', '\\"')
    return f'"{escaped}"'


def pascal(name: str) -> str:
    parts = re.findall(r"[A-Z]+(?=[A-Z][a-z]|\d|$)|[A-Z]?[a-z]+|\d+", name)
    return "".join(part if part.isupper() else part[:1].upper() + part[1:] for part in parts) or "Value"


def camel(name: str) -> str:
    class_name = pascal(name)
    if class_name.isupper():
        prop = class_name.lower()
    else:
        prop = class_name[:1].lower() + class_name[1:]
    return f"`{prop}`" if prop in KEYWORDS else prop


def build_tree() -> Node:
    root = Node("Vehicle")
    with SOURCE.open(newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            signal = row["Signal"]
            if not signal.startswith("Vehicle."):
                continue
            current = root
            for segment in signal.split(".")[1:]:
                current = current.children.setdefault(segment, Node(segment))
            if row["Type"] != "branch":
                current.data_type = row["DataType"]
                current.default = row["Default"]
    return root


def render_class(node: Node, class_name: str, indent: str = "") -> list[str]:
    lines: list[str] = []
    children = sorted(node.children.values(), key=lambda child: child.name)
    if children:
        nested = [child for child in children if not child.is_leaf]
        lines.append(f"{indent}data class {class_name}(")
        for child in children:
            prop = camel(child.name)
            if child.is_leaf:
                prop_type = kotlin_type(child.data_type)
                default = kotlin_default(child.data_type, child.default)
            else:
                prop_type = pascal(child.name)
                default = f"{prop_type}()"
            lines.append(f"{indent}    val {prop}: {prop_type} = {default},")
        if nested:
            lines.append(f"{indent}) {{")
            for index, child in enumerate(nested):
                if index > 0:
                    lines.append("")
                lines.extend(render_class(child, pascal(child.name), indent + "    "))
            lines.append(f"{indent}}}")
        else:
            lines.append(f"{indent})")
    else:
        lines.append(f"{indent}data class {class_name}(")
        lines.append(
            f"{indent}    val value: {kotlin_type(node.data_type)} = {kotlin_default(node.data_type, node.default)},",
        )
        lines.append(f"{indent})")
    return lines


def write_file(path: Path, lines: list[str]) -> None:
    path.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")


def main() -> None:
    root = build_tree()
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for old in OUTPUT.glob("Vss*.kt"):
        old.unlink()

    header = [
        "// Generated from docs/vss.csv.",
        "// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py",
        "",
        f"package {PACKAGE}",
        "",
    ]

    write_file(OUTPUT / "VssSignals.kt", header + render_class(root, "VssSignals"))
    for child in sorted(root.children.values(), key=lambda item: item.name):
        write_file(OUTPUT / f"Vss{pascal(child.name)}.kt", header + render_class(child, f"Vss{pascal(child.name)}"))


if __name__ == "__main__":
    main()
