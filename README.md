# SvgToImage!
A program to convert SVG images to PNG/JPEG/TIFF/TGA, written in Java using the Processing creative coding framework!

Processing Libraries made by very the friendly community this program puts to use!:
- [Drop `1.0.2`](http://transfluxus.github.io/drop/)
- [UiBooster `1.15.2`](https://github.com/milchreis/UiBooster)

:)

This was probably *the second time* in my life I wrote actual software that could be used by *the common man* on a day-to-day basis.

# Licensing:
The "GNU GPLv3" is the license this program is distributed under.
You may also refer to newer versions of the GNU GPL!

![License Terms, SIMPLIFIED!](https://user-images.githubusercontent.com/69293652/194107886-c5a3e0b1-86fc-470c-94fc-9428d9ca3cc3.png)


One instance of the license itself is available at https://raw.githubusercontent.com/Brahvim/SvgToImage/main/LICENSE.
**You should get it in the zip file you download.**

# Things needed before installation:
Please make sure you have Java installed! The Oracle JDK you would usually get from https://java.com/download *should* work, but I recommend using OpenJDK binaries from https://adoptium.net/temurin/releases.

This should fix the application not starting up at all.


**Please make sure the "Package Type" is "JRE" and you have chosen correct settings for your computer!**
![Adoptium website](https://user-images.githubusercontent.com/69293652/194105344-23ead1ee-a611-45f9-90c1-e652f3764f86.png)

You are recommended to use the latest version of the JRE, but for guaranteed compatibility with this application, you may choose to use JRE 8.

# How to install SvgToImage:
- Please select a version from:
https://github.com/Brahvim/SvgToImage/tags

- Unzip the file you downloaded,
- Open the folder you got from unzipping. You may see another folder named the same. If so, open it as well.
- You will see a file with the `.jar` file extension. This is the program.
- **For your convenience**, please move the folder the JAR file is in, along with all of its other contents, to a folder such as `C:/Windows/Program Files` (or whatever is equivalent on the operating system you are using), or some other folder of your choice other than your `Downloads` folder, since it _is usually_ filled with clutter.

# How to use:
Drag-and-drop an SVG file (`.svg` extension only!) onto this window:
![Drag and drop only onto this window!](https://user-images.githubusercontent.com/69293652/194111284-011cf772-ce3a-4e94-b703-44deb37cf892.png)


# Troubleshooting:

# Plans:
- CLI Mode! Since I provide the application as a single JAR file and not as a [Launch4j](https://launch4j.sourceforge.net/) wrapped executable, 
  I can *actually* make a command line version, code for which is can bee seen in [`CliMode.pde`](https://github.com/Brahvim/SvgToImage/blob/main/CliMode.pde)
