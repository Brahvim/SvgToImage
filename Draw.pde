void draw() {
  if (optionsForm != null)
    if (optionsForm.isClosedByUser())
      exit();

  if (exportName == null)
    surface.setTitle("SVG To Image Tool");
  else surface.setTitle("SVG To Image Tool - ".concat(exportName));

  background(0);

  if (appState == AppState.DISPLAY && pappState == AppState.EMPTY) {
    optionsForm = optionsFormBuild.run();

    optionsForm.getById("checkbox_style").setValue(Boolean.FALSE);
    optionsForm.getById("textarea_width").setValue("400");
    optionsForm.getById("textarea_height").setValue("400");
    optionsForm.getById("selection_format").setValue("PNG");
    // ^^^ Remember that the options Processing provides us are:
    // `PNG`, `JPEG`, `TIFF` and `TGA`.

    optionsWin = optionsForm.getWindow();
    optionsWin.setSize(320, 400); //420); // Add another `20` for `btn_save_to`. 
    optionsWin.setLocation(new Point(
      displayWidth / 2 + INIT_WIDTH / 2, displayHeight / 2 - INIT_HEIGHT / 2));
  }

  switch(appState) {
  case EMPTY:
    // Don't care about optimization, we're doing Computer Graphics and
    // this is supposed to be super-scalable non-game application code!
    text("Please drag-n-drop an SVG file here!", 
      cx, cy); // Use an accelerator table like all other Windows applications...?
    break;
  case DISPLAY:
    // Yep! Keep Re-rendering at 30 FPS.

    if (render == null) {
      text("Preview error, try\nediting a value\nto refresh...", 
        cx, cy); // Use an accelerator table like all other Windows applications...?
    } else if (previewTooLarge) {

      // If the preview is too large,

      if (Boolean.valueOf(optionsForm.getById("checkbox_style").asString()))
        text("Preview too large!\n" + render.width + "x" + render.height + "\n(Disabled Styling.)", 
          cx, qy);
      else
        text("Preview too large!\n" + render.width + "x" + render.height, cx, cy);
    } else try {

      // The image can be rendered now! All checks passed! :white_check_mark:

      image(render, cx, cy);
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      text("Preview error, try\nediting a value\nto refresh...", 
        cx, cy); // Use an accelerator table like all other Windows applications...?
    }
    // ^^^ Putting this in a `synchronized` block is *probably* useless
    // since the object might get passed around several times...
    break;

  default:// ...also applies to `CLI`!
  }
}
