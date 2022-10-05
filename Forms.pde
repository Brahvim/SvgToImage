FormBuilder optionsFormBuild;
Form optionsForm;
JDialog optionsWin;

void createOptionsForm() {
  optionsFormBuild = ui.createForm("Options");

  optionsFormBuild.addTextArea("Width:").setID("textarea_width");
  optionsFormBuild.addTextArea("Height:").setID("textarea_height");

  optionsFormBuild.addCheckbox("Disable Style?").setID("checkbox_style");
  optionsFormBuild.addSelection("File Format:", 
    "PNG", "JPEG", "TIFF", "TGA").setID("selection_format");

  optionsFormBuild.addButton("Export and view.", new Runnable() {
    public void run() {
      File outFile = new File(dataFolder.getAbsolutePath()
        .concat(File.separator)
        .concat(exportName.substring(0, exportName.lastIndexOf(".") + 1))
        .concat(exportFormat));

      // Check if a certain duplicate exists.
      // Java does not have any `uint32`!
      // I simply... won't use `long`, LOL.
      if (outFile.exists())
      for (int i = 1;; i++) {
        if (!(outFile = new File(dataFolder.getAbsolutePath()
          .concat(File.separator)
          .concat(exportName.substring(0, exportName.lastIndexOf(".")))
          .concat(" (" + i + ")")
          .concat(".")
          .concat(exportFormat))).exists())
        break;
      }

      synchronized(render) {
        if (render != null)
        render.save(outFile.getAbsolutePath());
      }

      exec("explorer", dataFolder.getAbsolutePath());
    }
  }
  ).setID("btn_save");

  /*
  optionsFormBuild.addButton("Export to... and view.", new Runnable() {
   File out;
   public void run() {
   out = ui.showFileOrDirectorySelection();
   //out = ui.showDirectorySelectionFromPath(dataFolder.getAbsolutePath());
   
   if (out == null)
   return;
   
   boolean selectionWasFile = false;
   
   if (out.isDirectory()) {
   println("Exporting to directory: ", out.getAbsolutePath(), " with name: ", exportName);
   out = new File(out.getAbsolutePath()
   .concat(File.separator)
   .concat(exportName.substring(0, exportName.lastIndexOf(".") + 1)) // Include the `.`!
   .concat(exportFormat));
   
   if (out.exists())
   for (int i = 1;; i++) {
   if (!(out = new File(out.getAbsolutePath()
   .concat(File.separator)
   .concat(exportName.substring(0, exportName.lastIndexOf(".")))
   .concat(" (" + i + ")")
   .concat(".")
   .concat(exportFormat))).exists())
   break;
   }
   } else if (out.getName().equals(exportName) && out.exists()) {
   selectionWasFile = true;
   println("Exporting as a file:", exportName, exportFormat);
   
   if (!ui.showConfirmDialog("Are you sure you want to overwrite `"
   .concat(out.getPath()).concat("`?"), "Overwrite file?"))
   return;
   
   } else selectionWasFile = true;
   
   synchronized(render) {
   if (render != null)
   if (selectionWasFile)
   render.save(out.getAbsolutePath());
   else
   render.save(out.getAbsolutePath()
   .concat(File.separator)
   .concat(exportName.substring(0, exportName.lastIndexOf(".") + 1))
   .concat(exportFormat));
   }
   
   if (selectionWasFile)
   exec("explorer", out.getParent());
   else
   exec("explorer", out.getAbsolutePath());
   }
   }
   ).setID("btn_save_to");
   */

  optionsFormBuild.setChangeListener(new FormElementChangeListener() {
    // These store the previous values entered in the form:
    int widthVal = DEFAULT_IMAGE_DIM, 
      heightVal = DEFAULT_IMAGE_DIM;
    // ^^^ **Should be made global variables if needed!**

    public void onChange(FormElement p_ele, Object p_data, Form p_form) {
      // "mtSpace4you!"
      // Readable? *Good.*

      switch (p_ele.getId()) {

      case "checkbox_style":
        if (Boolean.valueOf(p_ele.asString())) 
        svg.disableStyle();
        else svg.enableStyle();

        synchronized(render) {
          drawSvgToClearedBuffer(svg, render);
        }

        break;

      case "textarea_width":
        try {
          String valStr = p_ele.asString();
          if (valStr == "" || valStr == null)
          return;
          int gotVal = Integer.valueOf(valStr);

          if (gotVal == widthVal)
          return;

          // Do the rest of the error checks later so you do less checking when you return anyway :|
          if (gotVal > MAX_DIM)
          gotVal = MAX_DIM;

          widthVal = gotVal;
          println("Width change:", "`" + valStr + "`");
        }
        catch (NumberFormatException e) {
        }

        if (widthVal < 1)
        widthVal = DEFAULT_IMAGE_DIM;

        // Yes. Keep the OTHER one in sync!:
        optionsForm.getById("textarea_height").setValue(heightVal);

        synchronized(render) {
          render = createGraphics(widthVal, render.height);
          drawSvgToClearedBuffer(svg, render);
        }

        previewTooLarge = widthVal > displayWidth || heightVal > displayHeight;
        //widthVal * render.height >
        //(displayWidth - displayWidth / 6) * (displayHeight - displayHeight / 6)

        if (!previewTooLarge)
        surface.setSize(widthVal + 10, render.height + 10);
        else 
        surface.setSize(DEFAULT_IMAGE_DIM, DEFAULT_IMAGE_DIM);
        setWindowsInPlace();

        break;

      case "textarea_height":
        try {
          String valStr = p_ele.asString();
          if (valStr == "" || valStr == null)
          return;

          // Why keep these separated? :P
          int gotVal = Integer.valueOf(valStr);

          if (gotVal == heightVal)
          return;

          // Do the rest of the error checks later so you do less checking when you return anyway :|
          if (gotVal > MAX_DIM)
          gotVal = MAX_DIM;

          heightVal = gotVal;
          println("Height change:", "`" + valStr + "`");
        }
        catch (NumberFormatException e) {
        }

        if (heightVal < 1)
        heightVal = DEFAULT_IMAGE_DIM;

        // Yes. Keep the OTHER one in sync!:
        optionsForm.getById("textarea_width").setValue(widthVal);

        synchronized(render) {
          render = createGraphics(render.width, heightVal);
          drawSvgToClearedBuffer(svg, render);
        }

        previewTooLarge = widthVal > displayWidth || heightVal > displayHeight; 
        //heightVal * render.width >
        //(displayWidth - displayWidth / 6) * (displayHeight - displayHeight / 6);

        if (!previewTooLarge)
        surface.setSize(render.width + 10, heightVal + 10);
        else surface.setSize(DEFAULT_IMAGE_DIM, DEFAULT_IMAGE_DIM);
        setWindowsInPlace();

        break;

      case "selection_format":
        // Readable, READABLE!
        exportFormat = p_ele.asString().toLowerCase();
        println("Changed format to:", "`" + (exportFormat) + "`");
        break;
      }
    }
  }
  );

  // Was trying to remove the icon from here by calling `optionsWin.setUndecorated(true);`
  // ...to prevent the user from closing the form.
  // I'll just close the application from now on.

  //JDialog optionsWin = optionsForm.getWindow();
  // `optionsWin.setDisplayable(false);`?
  //optionsWin.removeNotify();
  //optionsWin.addNotify();

  // Imagine the program not continuing to `draw()` O_O
  //if (optionsForm.isClosedByUser())
  //exit();
  // Oh wait, it isn't even present yet!
}
