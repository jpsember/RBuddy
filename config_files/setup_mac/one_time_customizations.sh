#!/bin/bash

# Customize OSX
#
# Change grab file format:
defaults write com.apple.screencapture type pdf
#
# Stop backing up ipad to computer:
defaults write com.apple.iTunes DeviceBackupsDisabled -bool true
#
# Hide 'last login' message:
touch ~/.hushlogin
#
# Tell OSX not to create .DS_Store files on network drives
defaults write com.apple.desktopservices DSDontWriteNetworkStores true
#
#defaults write com.apple.Finder AppleShowAllFiles YES
defaults write com.apple.Safari ApplePersistenceIgnoreState YES
defaults write com.apple.Preview ApplePersistenceIgnoreState YES
defaults write com.apple.CrashReporter DialogType none
defaults write com.apple.dock workspaces-swoosh-animation-off -bool YES
defaults write com.apple.dock no-bouncing -bool TRUE

#
# Disable that stupid 'downloaded from internet' warning
#
defaults write com.apple.LaunchServices LSQuarantine -bool NO

# Show the ~/Library folder
# chflags nohidden ~/Library/
#

# Boneheads at Apple disabled autorepeat of certain characters
defaults write -g ApplePressAndHoldEnabled -bool false

# Make single page mode the default preview mode (may not actually work anymore)
defaults write com.apple.Preview PVPSDisplayMode 1
defaults write com.apple.Preview PVPDFDisplayMode 1

cp gitignore_global.txt ~/.gitignore_global
cp gitconfig.txt ~/.gitconfig
cp git-completion.bash.txt ~/.git-completion.bash
cp bash_login.txt ~/.bash_login
cp bash_profile.txt ~/.bash_profile
# cp com.apple.Terminal.plist.txt ~/Library/Preferences/com.apple.Terminal.plist
echo "You must copy the com.apple.Terminal.plist file to ~/Library/Preferences after closing Terminal and by using Finder / go to directory"

killall SystemUIServer
killall Dock


