#!/bin/bash
set -e

# Define variables
# We use a local directory because /usr/lib is not writable in this environment
ANDROID_HOME=$HOME/android-sdk
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
CMDLINE_TOOLS_ZIP="commandlinetools-linux.zip"

mkdir -p $ANDROID_HOME/cmdline-tools

# Download command line tools
echo "Downloading Android Command Line Tools..."
wget -q -O $CMDLINE_TOOLS_ZIP $CMDLINE_TOOLS_URL

# Unzip
echo "Unzipping..."
unzip -q $CMDLINE_TOOLS_ZIP -d $ANDROID_HOME/cmdline-tools

# Rename to 'latest'
if [ -d "$ANDROID_HOME/cmdline-tools/cmdline-tools" ]; then
    mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest
fi

# Clean up zip
rm $CMDLINE_TOOLS_ZIP

# Set environment variables locally for this script
export ANDROID_HOME=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Accept licenses and install platform tools and platforms
echo "Accepting licenses and installing components..."
yes | sdkmanager --licenses >/dev/null 2>&1
echo "Installing platform-tools, platforms;android-34, build-tools;34.0.0..."
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "Android SDK installed successfully at $ANDROID_HOME"
