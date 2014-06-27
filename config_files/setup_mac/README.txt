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

Finder preferences
[] Show these items on the desktop: Hard Disks, External Disks
[] New finder windows shows: home directory
[] sidebar: Favorites: Downloads, home directory
[] hide tags

[] Advanced: show all filename extensions
[] Set list view as default finder window view type

Open TextEdit and set its preferences
[] plain text; no spell check; Monaco 18pt font

Terminal preferences
[] Open Terminal, and set 'keep in dock'
[] Preferences / Settings: create new profile;
[] Startup: on startup, open New Window with settings 'Jeff';
[] Text: font: Monaco 18 pt;
[] Window: Background color: choose light gray
[] Window Size: 140 columns, 35 rows
[] Advanced: turn off audible bell

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
Use Shift-F11 to create three desktops in spaces

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
[] brew update
[] brew tap homebrew/dupes  (not sure this is required)
   choose an appropriate version e.g. 2.1.0, and type:
[] rbenv install 2.1.0
[] rbenv rehash
[] rbenv global 2.1.0
[] quit and rerun Terminal, type 'ruby -v' to verify 2.1.0 is active
[] Create a 'my_gems' directory in Downloads and go to it.  Then clone some repos:
[] git clone https://github.com/jpsember/js_base
[] git clone https://github.com/jpsember/cmd_line_tools
[] git clone https://github.com/jpsember/backup_set
[] git clone https://github.com/jpsember/git_repo
[] git clone https://github.com/jpsember/git_diff

   from the js_base directory, type
[] gem build js_base.gemspec
[] gem install js_base-1.0.0.gem

   do similar operation from the cmd_line_tools directory; then, from the my_gems directory,
   you ought to be able to type
[] makegem backup_set
[] makegem git_repo
[] makegem git_diff







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

Install the Android SDK
[] go to developer.android.com/sdk
[] Download the SDK (ADT Bundle for Mac)
[] Unzip it, and move the unzipped folder (adt-bundle-macXXXX) to the ~/android_development dir
 Make some symbolic links:
[] cd ~/android_development
[] ln -s adt-bundle-mac-XXXX/ adt
[] ln -s adt/eclipse/plugins/org.junit_4.11.0.v201303080030/ junit4

[] Use spotlight to run the Eclipse.app program; install the Java SE 6 runtime if it asks.
[] set Eclipse 'keep in dock' flag
[] choose ~/android_development/android_base as the workspace, and use as default
We're not storing Eclipse workspaces in the repo (only the projects), so some of these
steps are necessary:
[] import general/preferences from android_base/config_files/jeff.epf; you may have to fix
  some paths that refer to my home directory.
[] in Preferences / Java / Editor / Templates, import config_files/templates.xml

[] import existing projects into workspace; choose all of them
[] show the view Android/LogCat, and choose the System.out filter
[] In Package Explorer, create a filter; check 'Name filter patterns' and
   enter the patterns 'link_*,*?src,src_*,project.properties'; check 'Libraries from external'

Install some other tools
[] VLC (google 'videolan osx')


I think these are necessary for Android development:
[] brew install ant
[] copy junit3.jar to a directory named junit3 within the ~/android_development directory



