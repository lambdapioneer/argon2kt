#!/bin/bash
set -e

# Use this script to update the sources to the latest release of the original
# argon2 project: https://github.com/P-H-C/phc-winner-argon2/releases
#
# Sample usage: ./update_argon2_sources.sh 20190702

if [[ $# -eq 0 ]]; then
    echo "Usage: ./update_argon2_sources.sh <tag of latest release>"
    exit 1
fi

tmpdir=".tmp_argon2_sources_update"
tag=$1
uri="https://github.com/P-H-C/phc-winner-argon2/archive/$tag.tar.gz"

echo "[ ] Creating temporary working directory..."
mkdir -p "$tmpdir"

sourcestar="$tmpdir/sources.tar.gz"
echo "[ ] Downloading the archive..."
wget -nv "$uri" -O "$sourcestar"

sourcestarfolder="phc-winner-argon2-$tag/src"
echo "[ ] Extracting the sources..."
tar -f "$sourcestar" -C "$tmpdir" -x "$sourcestarfolder"

sourcesfolder="$tmpdir/$sourcestarfolder"
destination="lib/src/main/cpp/argon2/"
echo "[ ] Copying sources to $destination..."
cp -rv "$sourcesfolder"/* "$destination"

echo "[ ] Removing temporary working directory..."
rm -R "$tmpdir"

echo "[+] Done. You should run './gradlew clean assembleDebug' now to verify the changes. Do not forget to test as well"
