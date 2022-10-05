
// Sadly, this is broken.


/**
 How to use CLI Mode!: (Just... take all the data you need, please. Don't make so many formats!)
 
 
 Command format:
 `SvgToImage <width> <height> <boolean-style> "<path-to-file>" "<export-path>"`.
 
 That's literally only one format. Both simple for me, and every user to rememeber :)
 
 Also, `SvgToImage -help` will print that out! :D
 
 
 ...the old stuff:
 
 DEPRECATED!: ~~`SvgToImage <side> "<path-to-file>"`,~~
 DEPRECATED!: ~~`SvgToImage <boolean-style> <side> "<path-to-file>"`,~~
 
 `SvgToImage <width> <height> "<path-to-file>"`,
 `SvgToImage <boolean-style> <width> <height> "<path-to-file>"`,
 
 `SvgToImage <side> "<path-to-file>" "<export-path>"`,
 `SvgToImage <boolean-style> <side> "<path-to-file>" "<export-path>"`,
 
 `SvgToImage <width> <height> "<path-to-file>" "<export-path>"`,
 `SvgToImage <boolean-style> <width> <height> "<path-to-file>" "<export-path>"`.
 
 `<export-path>` should end with the extension of the user's choice.
 If it starts with a `.`, and has the file extension written after (e.g. `.png`),
 then it is interpreted as the file extension, and the file is placed at the default path.
 :)
 */

// ...yeah, used a *bit* of functional programming here or something.
// The arguments passed to the sketch are not needed by the GUI, are they?
// It's not stored globally, and I won't do this in a class and all.
// Functional programming #FTW *here only at least! ..and most!:*

void cliMode() {
  String sketchArgsStr = System.getProperty("sun.java.command");
  String[] sketchArgs = split(sketchArgsStr, " ");

  int commandStart = getCommandStart(sketchArgs);

  // Thanks to all the functional programming,
  // this has started to look a lot like C/C++!

  // PS I finally understood the proper definition for a class while writing that:
  // Classes are interfaces (APIs) to get work done by.
  // Don't use them just for storing data. If you do, treat them TRULY like `struct`s.
  // Use static methods to modify them, and all'a ("all of") that.

  boolean check = inCliMode(sketchArgs, commandStart); // Have this here for scalability.
  // An `if-else` repeating the following `println()` statement would do fine, 
  // but scalability matters! This is a real application's code! HUSH!
  println("We're in", check? "CLI" : "GUI", "mode!");

  if (check) {
    appState = AppState.CLI;
    cliImpl(sketchArgs, commandStart);
  }
}

int getCommandStart(String[] p_args) {
  int commandStart;
  for (commandStart = 0; commandStart < p_args.length; commandStart++)
    if (p_args[commandStart].equals(SKETCH_NAME))
      break;

  println("Command reading starts after index:", commandStart);
  return commandStart;
}

// Better to pass the entire array here - what if this needs changes in the future?
// PS `getCommandStart()` exists solely to disallow doing the same calculation more than once.
// Future changes could force us to break the functional pattern...
// Use OOP in that case! Classes to the rescue! Caching! *No more globals!*

boolean inCliMode(String[] p_args, int p_commandStart) {
  return p_commandStart + 1 != p_args.length; // Addition is just easier for machines.
  // ^^^ If it is *equal* to `p_args.length - 1`, it'd mean that there were no
  // more commands passed, indicating that the application was in GUI mode.
  // It certainly cannot be greater! Which thread added that extra element?!
}

void cliImpl(String[] p_commands, int p_commandStart) {
  surface.setVisible(false); // Since this is a Java window, it *does* actually hide.
  // No idea what's wrong with OpenGL ones :|

  /*
   `commandStart` LOL : width!
   `commandStart + 1` : height!
   `commandStart + 2` : style!
   `commandStart + 3` : file!!!
   `commandStart + 4` : path to export to!1!!11!!1!
   */

  if (p_commands.length - 6 != p_commandStart) {
    ui.showErrorDialog("Command did not have enough arguments!", "Uhh...");
    exit();
  }

  int pw = -1, ph = -1;
  boolean style = false;
  File inputFile = null, exportFile = null;

  for (int i = p_commandStart; i < p_commands.length; i++) {
    String cmd = p_commands[i];

    switch (p_commands.length - i) {
      // These are in reverse order, so I reversed the numbers for you!:
    case 4:
      try {
        pw = Integer.valueOf(cmd);
      }
      catch (NumberFormatException e) {
        exit();
      }

      if (pw < 0)
        exit();

      break;

    case 3:
      try {
        ph = Integer.valueOf(cmd);
      }
      catch (NumberFormatException e) {
        exit();
      }

      if (ph < 0)
        exit();

      break;

    case 2:
      String cmdLow = cmd.toLowerCase();
      if (cmdLow.equals("true") || cmdLow.equals("false"))
        style = Boolean.valueOf(cmd);
      else exit();
      break;

    case 1:
      inputFile = new File(removeQuotesAndSlashes(cmd));
      if (!inputFile.exists())
        exit();
      break;

    case 0:
      exportFile = new File(removeQuotesAndSlashes(cmd));
      if (!exportFile.exists())
        exit();
      break;
    }
  }

  ui.showInfoDialog("Processing...");

  render = createGraphics(pw, ph);

  if (!PApplet.getExtension(inputFile.getAbsolutePath()).equals("svg")) {
    ui.showErrorDialog("The file you gave me is not even an SVG.", "-_-");
    return;
  }

  File dataFile = new File(dataFolder, "svg.svg");
  boolean copyState = svgFileCopyCli(inputFile, dataFile);

  if (!copyState) {
    dataFile.delete();
    exit();
  }

  svg = loadShape("svg.svg");
  dataFile.delete();

  if (!style)
    svg.disableStyle();

  drawSvgToClearedBuffer(svg, render);

  render.save(exportFile.getAbsolutePath().concat(
    exportFile.isDirectory()? File.separator.concat(exportName): ""));

  ui.showInfoDialog("Check ya' desktop :D");
  exit();
}

String removeQuotesAndSlashes(String p_str) {
  return p_str.replaceAll("\"", " ").replaceAll("/", File.separator);
  //return p_str.substring(1, p_str.length() - 1); // BAD! >:(
}
