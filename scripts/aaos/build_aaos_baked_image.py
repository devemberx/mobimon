#!/usr/bin/env python3
"""Build a local AAOS image with the MobiMon system bars overlay preinstalled."""

import argparse
import hashlib
import os
import re
import shutil
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path


SOURCE_ROOT = Path("system-images/android-34-ext9/android-automotive")
SOURCE_SIZE = 5859442688
# Exact revision 5 disk layouts. The SHA-256 guard prevents applying these
# product offsets to another release or a locally modified SDK image.


@dataclass(frozen=True)
class ImageProfile:
    sha256: str
    product_offset: int
    product_size: int


IMAGE_PROFILES = {
    "arm64-v8a": ImageProfile(
        "52091ab58529c97e397ffb96f52b9421710d280acc7cad470b0b15ba90511ddf",
        (4096 + 3108864) * 512,
        1991592 * 512,
    ),
    "x86_64": ImageProfile(
        "5507f2616ce60411122845d4db771aa3d6dc74450ea48e33a3a50504438e7adb",
        1614807040,
        267424 * 4096,
    ),
}

# The final byte of the AVB header flags disables hashtree, as adb disable-verity
# does on this userdebug image. The source hash above guards this fixed offset.
VBMETA_FLAG_OFFSET = 2048 * 512 + 123
OVERLAY_NAME = "MobiMonSystemBars.apk"
SELINUX_LABEL = b"u:object_r:system_file:s0\0"


def file_sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(8 * 1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def copy_file(source, target):
    if os.uname().sysname == "Darwin":
        try:
            subprocess.run(["cp", "-c", str(source), str(target)], check=True, capture_output=True)
            return
        except subprocess.CalledProcessError:
            target.unlink(missing_ok=True)
    shutil.copy2(source, target)


def copy_region(source, target, offset, size):
    with source.open("rb") as disk, target.open("wb") as partition:
        disk.seek(offset)
        remaining = size
        while remaining:
            chunk = disk.read(min(8 * 1024 * 1024, remaining))
            if not chunk:
                raise ValueError("Source image ended before the product partition")
            partition.write(chunk)
            remaining -= len(chunk)


def patch_system_image(source, product, destination, product_offset, vbmeta_flag_offset):
    """Copy the disk, replace changed product blocks, and disable AVB hashtree."""
    if destination.exists():
        raise FileExistsError(destination)
    if product_offset + product.stat().st_size > source.stat().st_size:
        raise ValueError("Product partition is outside the source image")
    with source.open("rb") as disk:
        disk.seek(vbmeta_flag_offset - 123)
        if disk.read(4) != b"AVB0":
            raise ValueError("Expected an AVB vbmeta header")
        disk.seek(vbmeta_flag_offset)
        if disk.read(1) != b"\0":
            raise ValueError("Unexpected AVB hashtree flag")

    copy_file(source, destination)
    changed = 0
    with source.open("rb") as original, product.open("rb") as updated, destination.open("r+b") as result:
        original.seek(product_offset)
        for relative in range(0, product.stat().st_size, 4096):
            before = original.read(4096)
            after = updated.read(len(before))
            if before != after:
                result.seek(product_offset + relative)
                result.write(after)
                changed += 1
        result.seek(vbmeta_flag_offset)
        result.write(b"\x01")
    return changed


def validate_avd_template(template_config, abi):
    config = {}
    for line in template_config.read_text().splitlines():
        key, separator, value = line.partition("=")
        if separator:
            config[key] = value
    expected = {
        "abi.type": abi,
        "hw.lcd.width": "2560",
        "hw.lcd.height": "1440",
        "hw.lcd.density": "160",
    }
    for key, value in expected.items():
        if config.get(key) != value:
            raise ValueError(f"Template AVD must have {key}={value}")
    if "image.sysdir.1" not in config:
        raise ValueError("Template AVD is missing its system image path")


def create_avd(template_config, avd_home, image_dir, avd_name, abi="arm64-v8a", host_path=None):
    if not re.fullmatch(r"[A-Za-z0-9_]+", avd_name):
        raise ValueError("AVD name may contain only letters, digits, and underscores")
    avd_dir = avd_home / f"{avd_name}.avd"
    descriptor = avd_home / f"{avd_name}.ini"
    if avd_dir.exists() or descriptor.exists():
        raise FileExistsError(f"AVD already exists: {avd_name}")
    if not image_dir.is_dir():
        raise FileNotFoundError(image_dir)
    validate_avd_template(template_config, abi)
    if host_path is None:
        host_path = lambda path: str(path.resolve())

    replacements = {
        "AvdId": avd_name,
        "avd.ini.displayname": avd_name,
        "image.sysdir.1": f"{host_path(image_dir)}/",
    }
    lines = []
    for line in template_config.read_text().splitlines():
        key, separator, value = line.partition("=")
        if separator:
            if key in replacements:
                line = f"{key}={replacements[key]}"
        lines.append(line)
    for key in ("AvdId", "avd.ini.displayname"):
        if not any(line.startswith(f"{key}=") for line in lines):
            lines.insert(0, f"{key}={replacements[key]}")

    avd_home.mkdir(parents=True, exist_ok=True)
    avd_dir.mkdir()
    try:
        (avd_dir / "config.ini").write_text("\n".join(lines) + "\n")
        descriptor.write_text(
            "avd.ini.encoding=UTF-8\n"
            f"path={host_path(avd_dir)}\n"
            f"path.rel=avd/{avd_name}.avd\n"
            "target=android-34-ext9\n"
        )
    except BaseException:
        descriptor.unlink(missing_ok=True)
        shutil.rmtree(avd_dir)
        raise
    return avd_dir


def template_abi(template_config):
    for line in template_config.read_text().splitlines():
        if line.startswith("abi.type="):
            abi = line.partition("=")[2]
            if abi in IMAGE_PROFILES:
                return abi
            break
    raise ValueError("Template AVD must use arm64-v8a or x86_64")


def windows_path(path):
    if not os.environ.get("WSL_DISTRO_NAME"):
        raise RuntimeError("--windows-paths must be run inside WSL")
    result = subprocess.check_output(["wslpath", "-m", str(path.resolve())], text=True).strip()
    if not re.match(r"^[A-Za-z]:/", result):
        raise ValueError(f"AVD path must be on a Windows drive: {path}")
    return result


def find_tool(name):
    candidates = [
        shutil.which(name),
        f"/opt/homebrew/opt/e2fsprogs/sbin/{name}",
        f"/usr/sbin/{name}",
    ]
    for candidate in candidates:
        if candidate and Path(candidate).is_file():
            return str(candidate)
    raise FileNotFoundError(f"{name} is required; install e2fsprogs")


def run_debugfs(debugfs, image, command):
    result = subprocess.run(
        [debugfs, "-R", command, str(image)], capture_output=True, text=True, check=True
    )
    if "File not found" in result.stderr or "No such file" in result.stderr:
        raise RuntimeError(result.stderr.strip())
    return result.stdout


def add_overlay(product, apk, work, debugfs, e2fsck):
    if OVERLAY_NAME in run_debugfs(debugfs, product, "ls /overlay"):
        raise ValueError("Overlay is already present in the source image")
    local_apk = work / "overlay.apk"
    shutil.copy2(apk, local_apk)
    label = work / "selinux-label.bin"
    label.write_bytes(SELINUX_LABEL)
    write = subprocess.run(
        [debugfs, "-w", "-R", f"write {local_apk} /overlay/{OVERLAY_NAME}", str(product)],
        capture_output=True, text=True, check=True,
    )
    if "Allocated inode:" not in write.stdout:
        raise RuntimeError(write.stderr or write.stdout)
    subprocess.run(
        [debugfs, "-w", "-R",
         f"ea_set -f {label} /overlay/{OVERLAY_NAME} security.selinux", str(product)],
        capture_output=True, text=True, check=True,
    )
    stat = run_debugfs(debugfs, product, f"stat /overlay/{OVERLAY_NAME}")
    if "Mode:  0644" not in stat or "u:object_r:system_file:s0" not in stat:
        raise RuntimeError("Overlay permissions or SELinux label are incorrect")
    extracted = work / "extracted.apk"
    run_debugfs(debugfs, product, f"dump /overlay/{OVERLAY_NAME} {extracted}")
    if extracted.read_bytes() != apk.read_bytes():
        raise RuntimeError("Overlay bytes changed inside product image")
    subprocess.run([e2fsck, "-fn", str(product)], capture_output=True, text=True, check=True)


def build_image(sdk_dir, output_dir, apk, debugfs, e2fsck, abi="arm64-v8a"):
    profile = IMAGE_PROFILES[abi]
    source_dir = (sdk_dir / SOURCE_ROOT / abi).resolve()
    output_dir = output_dir.resolve()
    if source_dir == output_dir or source_dir in output_dir.parents:
        raise ValueError("Output image directory must be outside the source SDK image")
    source = source_dir / "system.img"
    if source.stat().st_size != SOURCE_SIZE or file_sha256(source) != profile.sha256:
        raise ValueError("Unsupported AAOS source image revision or modified SDK image")
    if output_dir.exists():
        raise FileExistsError(output_dir)
    output_dir.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="mobimon-aaos-build-") as temporary, \
            tempfile.TemporaryDirectory(
                prefix="mobimon-aaos-stage-", dir=output_dir.parent
            ) as staging:
        work = Path(temporary)
        product = work / "product.img"
        copy_region(source, product, profile.product_offset, profile.product_size)
        add_overlay(product, apk, work, debugfs, e2fsck)
        staged = Path(staging) / "image"
        staged.mkdir()
        for item in source_dir.iterdir():
            if item.name == "system.img":
                continue
            if item.is_dir():
                shutil.copytree(item, staged / item.name)
            else:
                copy_file(item, staged / item.name)
        changed = patch_system_image(
            source, product, staged / "system.img", profile.product_offset, VBMETA_FLAG_OFFSET
        )
        if changed == 0:
            raise RuntimeError("Product partition did not change")
        staged.rename(output_dir)
    return changed


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--sdk-dir", type=Path, default=Path(os.environ.get("ANDROID_HOME", "")))
    parser.add_argument("--output-image-dir", type=Path, required=True)
    parser.add_argument("--template-avd-config", type=Path, required=True)
    parser.add_argument("--avd-home", type=Path, default=Path.home() / ".android/avd")
    parser.add_argument("--avd-name", default="mobimon_baked_bars_34")
    parser.add_argument("--windows-paths", action="store_true", help="Write Windows AVD paths from WSL")
    args = parser.parse_args()
    if not args.template_avd_config.is_file():
        parser.error("--template-avd-config must point to an existing AVD config.ini")
    abi = template_abi(args.template_avd_config)
    validate_avd_template(args.template_avd_config, abi)
    if not re.fullmatch(r"[A-Za-z0-9_]+", args.avd_name):
        parser.error("AVD name may contain only letters, digits, and underscores")
    if not (args.sdk_dir / SOURCE_ROOT / abi).is_dir():
        parser.error(f"--sdk-dir must contain the AAOS 34-ext9 {abi} image")
    image_dir = args.output_image_dir.expanduser().resolve()
    avd_home = args.avd_home.expanduser().resolve()
    avd_dir = avd_home / f"{args.avd_name}.avd"
    if image_dir.exists() or avd_dir.exists() or (avd_home / f"{args.avd_name}.ini").exists():
        parser.error("Output image or AVD already exists; choose a new name/path")
    host_path = windows_path if args.windows_paths else None
    if host_path:
        host_path(image_dir)
        host_path(avd_home)

    environment = os.environ.copy()
    environment["ANDROID_HOME"] = str(args.sdk_dir.resolve())
    apk = Path(subprocess.check_output(
        ["bash", str(Path(__file__).resolve().with_name("build-aaos-system-bars-overlay.sh"))],
        text=True, env=environment,
    ).strip())
    changed = build_image(args.sdk_dir, image_dir, apk, find_tool("debugfs"), find_tool("e2fsck"), abi)
    create_avd(args.template_avd_config, avd_home, image_dir, args.avd_name, abi, host_path)
    print(f"Built {image_dir} ({changed} modified product blocks)")
    print(f"Created AVD {args.avd_name}; launch it with Device Manager Start.")


if __name__ == "__main__":
    main()
