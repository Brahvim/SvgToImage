import drop.*;

import java.awt.Frame;
import java.awt.Point;
import java.io.Closeable;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import javax.swing.JDialog;

//import processing.awt.PShapeJava2D;
//import processing.awt.PSurfaceAWT;
import processing.awt.PSurfaceAWT.SmoothCanvas;

import uibooster.*;
import uibooster.model.*;
//import uibooster.components.WindowSetting;

// Making the object here causes an AWESOME 'bug' (just a setting UiBooster does not reset)
// with the JVM, giving the *Processing window* dark theme, too!
// ...too sad that I might just remove the window decorations...
// (I didn't!)
UiBooster ui = new UiBooster(UiBoosterOptions.Theme.DARK_THEME);
SDrop drop;

// Rendering, input and export:
PGraphics render; 
// ^^^ Nice and scalable! Not using `g` is a good choice. At least I won't re-render!
PShape svg; // Load this up!
String exportFormat = "png", exportName;
boolean previewTooLarge;
File dataFolder;

// Dimensions:
final int MAX_DIM = 16384, DEFAULT_IMAGE_DIM = 400;
final int INIT_WIDTH = 600, INIT_HEIGHT = DEFAULT_IMAGE_DIM;
float cx, cy, qx, qy;
int pwidth, pheight;

// Yeah, yeah, whatever. NO SCENE SYSTEM!

public static enum AppState/*s*/ {
  EMPTY, DISPLAY, CLI
}

AppState appState = AppState.EMPTY, pappState;

/*PSurfaceAWT.*/SmoothCanvas window;
final String SKETCH_NAME = this.getClass().getSimpleName();

void settings() {
  size(INIT_WIDTH, INIT_HEIGHT);
}

void setup() {
  //cliMode();

  // Typography:
  textFont(createFont("SansSerif", 72));
  textAlign(CENTER);
  textSize(36); // Needs to be in order!

  // The "Drop" library:
  SDrop.DEBUG = false;
  drop = new SDrop(this);

  // Data folder path:
  dataFolder = new File(sketchPath("data".concat(File.separator)));
  // (DO NOT do this in the class! `setup()` is the only place where `sketchPath()` is correct!

  render = createGraphics(DEFAULT_IMAGE_DIM, DEFAULT_IMAGE_DIM);

  surface.setTitle("SVG to Image Tool");
  imageMode(CENTER);
  frameRate(30); // `30` and not `15` if I ever want to extend stuff. Haha.
  registerMethod("pre", this);
  registerMethod("post", this);

  window = (/*PSurfaceAWT.*/SmoothCanvas)surface.getNative();

  createOptionsForm();
}

void pre() {
  if (!(pwidth == width || pheight == height)) {
    cx = width * 0.5f;
    cy = height * 0.5f;
    qx = cx * 0.5f;
    qy = cy * 0.5f;
  }
}

void post() {
  pappState = appState;
}

void drawSvgToClearedBuffer(PShape p_svg, PGraphics p_buffer) {
  if (p_svg == null)
    throw new NullPointerException("`drawSvgToBuffer()` received a `null` `PShape` :|");

  if (p_buffer == null)
    new NullPointerException("`drawSvgToBuffer() received a `null` `PGraphics` :|`").printStackTrace();

  PGraphics b = p_buffer;

  b.beginDraw();
  b.clear(); // Remove this to make the function... `drawSvgToBuffer()` :P
  b.shape(p_svg, 0, 0, b.width, b.height);
  b.endDraw();
}

void inputFileError() {
  ui.showErrorDialog("File IO Error!", "Uhh...");
}

void closeStream(Closeable p_str) {
  try {
    p_str.close();
  }
  catch (IOException e) {
  }
}

void svgFileCopy(File p_from, File p_to) {
  // Copy the selected file to the sketchs data folder so Processing can actually load it
  // without hackery with `PApplet.loadShape()`'s `XML` class logic:
  FileInputStream iStr = null;
  FileOutputStream oStr = null;

  try {
    oStr = new FileOutputStream(p_to);
  }
  catch (IOException e) {
    inputFileError();
  }

  try {
    iStr = new FileInputStream(p_from);
  }
  catch (IOException e) {
    ui.showErrorDialog("That's not an SVG!", ":|");
    closeStream(oStr);
    return;
  }

  // Is copying an array faster (at the expense of memory, of course!)?
  // 'cause I'm gunna use that if it *is* so...
  // ..and I feel that it IS.

  // Realization: two loops are better than one no matter the number of functions calls.
  // Also, JIT! ...unless this is a native method, in which case,
  // ... 
  try {
    for (int i; (i = iStr.read()) != -1; )
      oStr.write(i);
  }
  catch (IOException e) {
  }

  closeStream(iStr);
  closeStream(oStr);
}


boolean svgFileCopyCli(File p_from, File p_to) {
  // Copy the selected file to the sketchs data folder so Processing can actually load it
  // without hackery with `PApplet.loadShape()`'s `XML` class logic:
  FileInputStream iStr = null;
  FileOutputStream oStr = null;

  try {
    oStr = new FileOutputStream(p_to);
  }
  catch (IOException e) {
    return false;
  }

  try {
    iStr = new FileInputStream(p_from);
  }
  catch (IOException e) {
    closeStream(oStr);
    return false;
  }

  // Is copying an array faster (at the expense of memory, of course!)?
  // 'cause I'm gunna use that if it *is* so...
  // ..and I feel that it IS.

  // Realization: two loops are better than one no matter the number of functions calls.
  // Also, JIT! ...unless this is a native method, in which case,
  // ...

  try {
    for (int i; (i = iStr.read()) != -1; )
      oStr.write(i);
  }
  catch (IOException e) {
    return false;
  }
  finally {
    closeStream(iStr);
    closeStream(oStr);
  }

  return true;
}


void resizeThenCenter(int p_x, int p_y) {
  window.setSize(p_x, p_y);
  centerWindow();
  setWindowsInPlace(); // I won't update the logic for that single line all the time :|
}

void setWindowsInPlace() {
  surface.setLocation(0, 0);
  optionsForm.getWindow().setLocation(width, 0);
}

void windowAtOrigin() {
  surface.setLocation(0, 0);
}

// Kinda' broken...
void centerWindow() {
  surface.setLocation((int)(displayWidth / 2 - cx), (int)(displayHeight / 2 - qy));
}

// To be used for cross-platform support.
//void showInExplorer() {
//}
