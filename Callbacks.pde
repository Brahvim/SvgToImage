void mousePressed() {
  if (svg != null)
    drawSvgToClearedBuffer(svg, render);
  println("User clicked! Refreshed buffer.");
}

void keyPressed() {
  if (keyCode == 27)
    key = '\0';
}

void dropEvent(DropEvent p_evt) {
  // I mean, this function is async anyway...

  // Alternative approach: copy the file to the sketch's `data` folder.

  if (!dataFolder.exists())
    dataFolder.mkdir();
  dataFolder.deleteOnExit();

  File realFile = new File(p_evt.toString()), 
    dataFile = new File(dataFolder, "svg.svg");

  // Fixes a bug which causes an `svg.svg` file to appear in the output directory. 
  if (!PApplet.getExtension(realFile.getAbsolutePath()).equals("svg"))
    return;

  String realFilePath = realFile.getPath();
  int realFileExtStart = realFilePath.lastIndexOf(".");

  if (realFileExtStart != -1)
    realFilePath.substring(realFileExtStart, realFilePath.length());

  if (!dataFile.exists())
  try {
    dataFile.createNewFile();
  }
  catch (IOException e) {
  }

  svgFileCopy(realFile, dataFile);

  //synchronized(svg) {} // Causes the image to just... *NOT LOAD!* (..or display! It causes lag, too...)
  svg = loadShape("svg.svg");

  exportName = realFile.getName();
  dataFile.delete();

  appState = AppState.DISPLAY;
  drawSvgToClearedBuffer(svg, render);
  optionsForm.getById("checkbox_style").setValue(new Boolean(false));

  surface.setSize(render.width, render.height);
  setWindowsInPlace();

  println("Loaded SVG file!");
}
