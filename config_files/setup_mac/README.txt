Steps to take when setting up a new Mac
----------------------------------------

[] turn on laptop
[] Use English for main language
[] United States, U.S. Keyboard Layout
[] network: choose other, fill in appropriate fields
[] don't transfer any information
[] don't sign in with any Apple ID
[] agree to license
[] choose account name, don't require password to unlock screen
[] don't send diagnostics
[] don't register

[] remove all icons from dock except iTunes
Preferences
[] change desktop background to solid blue
[] Desktop & Screen Saver / choose screen saver with empty message (actually, a space)
[] Dock: Automatically hide and show the dock
[] Mission Control: uncheck 'show dashboard as a space'
[] uncheck 'Automatically rearrange Spaces...'
Keyboard and mouse shortcuts
[] Change Mission Control to Shift F11
[] Remove Application Windows
[] Remove Show Dashboard
[] Hot Corners: bottom right = Start Screen Saver
Keyboard
[] Key repeat: Fast(est); delay until repeat: second from shortest
[] Use all F1,F2 etc as standard function keys
Mouse
[] Uncheck scroll direction: natural
Sound
[] Uncheck play user interface sound effects
[] Uncheck show volume in menu bar
Bluetooth
[] Uncheck show bluetooth in menu bar
Time machine
[] uncheck show time machine in menu bar

[] Use spotlight to start safari, install chrome; drag to App folder as
  suggested; start from spotlight; set as default browser; eject Google Chrome
[] use Chrome to search for 'install homebrew'
[] open Terminal window and select 'keep in dock'
[] paste in the 'ruby -e ...' thing to install Homebrew
[] type password as required
[] install XCode tools as required
[] brew doctor
[] brew update
[] brew install rbenv
[] brew install ruby-build
[] ruby-build --definitions
[] choose an appropriate version e.g. 2.1.0, and type:
[] rbenv install 2.1.0
[] rbenv rehash

Set up Password-less login
[] ssh-keygen
[] accept default directory (.../id_rsa)
[] use account password for passphrase


GitHub login
[] log into your GitHub account
[] from settings, select SSH Keys
[] delete any old key, and add the one generated above with title e.g. "Tim's laptop"
[] type 'cat ~/.ssh/id_rsa.pub' and copy it to the browser window


[] cd ~
[] mkdir android_development
[] cd android_development
[] git clone git@github.com:jpsember/android_base.git

[] cd to ~/android_development/android_base/config_files/setup_mac
[] type:  bash one_time_customizations.sh

