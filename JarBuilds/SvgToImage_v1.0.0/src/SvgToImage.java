import java.awt.Point;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JDialog;

import drop.DropEvent;
import drop.SDrop;
import processing.awt.PSurfaceAWT.SmoothCanvas;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import uibooster.UiBooster;
import uibooster.model.Form;
import uibooster.model.FormBuilder;
import uibooster.model.FormElement;
import uibooster.model.FormElementChangeListener;
import uibooster.model.UiBoosterOptions;

// ...wish I could modify VSCode to let me write comments without a space
// before the `//` for "code comments". IntelliJ is just too hard for me to use :joy:

public class SvgToImage extends PApplet {
    public static void main(String[] p_args) {
        SvgToImage app = new SvgToImage();
        String[] appArgs = new String[] { app.getClass().getName() };

        if (p_args != null)
            PApplet.concat(appArgs, p_args);

        PApplet.runSketch(appArgs, app);

        // if (p_args != null) {
        // PApplet.main(concat(appArgs, p_args));
        // } else {
        // PApplet.main(appArgs);
        // }
    }

    // Making the object here causes an AWESOME 'bug' (just a setting UiBooster does
    // not reset)
    // with the JVM, giving the *Processing window* dark theme, too!
    // ...too sad that I might just remove the window decorations...
    // (I didn't!)
    UiBooster ui = new UiBooster(UiBoosterOptions.Theme.DARK_THEME);
    SDrop drop;

    // Rendering, input and export:
    PGraphics render;
    // ^^^ Nice and scalable! Not using `g` is a good choice. At least I won't
    // re-render!
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

    public static enum AppState/* s */ {
        EMPTY, DISPLAY, CLI
    }

    AppState appState = AppState.EMPTY, pappState;

    /* PSurfaceAWT. */SmoothCanvas window;
    final String SKETCH_NAME = this.getClass().getSimpleName();

    public void settings() {
        size(INIT_WIDTH, INIT_HEIGHT);
    }

    public void setup() {
        // cliMode();

        // Typography:
        textFont(createFont("SansSerif", 72));
        textAlign(CENTER);
        textSize(36); // Needs to be in order!

        // The "Drop" library:
        SDrop.DEBUG = false;
        drop = new SDrop(this);

        // Data folder path:
        dataFolder = new File(sketchPath("data".concat(File.separator)));
        // (DO NOT do this in the class! `setup()` is the only place where
        // `sketchPath()` is correct!

        render = createGraphics(DEFAULT_IMAGE_DIM, DEFAULT_IMAGE_DIM);

        surface.setTitle("SVG to Image Tool");
        imageMode(CENTER);
        frameRate(30); // `30` and not `15` if I ever want to extend stuff. Haha.
        registerMethod("pre", this);
        registerMethod("post", this);

        window = (/* PSurfaceAWT. */SmoothCanvas) surface.getNative();

        createOptionsForm();
    }

    public void pre() {
        if (!(pwidth == width || pheight == height)) {
            cx = width * 0.5f;
            cy = height * 0.5f;
            qx = cx * 0.5f;
            qy = cy * 0.5f;
        }
    }

    public void post() {
        pappState = appState;
    }

    public void drawSvgToClearedBuffer(PShape p_svg, PGraphics p_buffer) {
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

    public void inputFileError() {
        ui.showErrorDialog("File IO Error!", "Uhh...");
    }

    public void closeStream(Closeable p_str) {
        try {
            p_str.close();
        } catch (IOException e) {
        }
    }

    public void svgFileCopy(File p_from, File p_to) {
        // Copy the selected file to the sketchs data folder so Processing can actually
        // load it
        // without hackery with `PApplet.loadShape()`'s `XML` class logic:
        FileInputStream iStr = null;
        FileOutputStream oStr = null;

        try {
            oStr = new FileOutputStream(p_to);
        } catch (IOException e) {
            inputFileError();
        }

        try {
            iStr = new FileInputStream(p_from);
        } catch (IOException e) {
            ui.showErrorDialog("That's not an SVG!", ":|");
            closeStream(oStr);
            return;
        }

        // Is copying an array faster (at the expense of memory, of course!)?
        // 'cause I'm gunna use that if it *is* so...
        // ..and I feel that it IS.

        // Realization: two loops are better than one no matter the number of functions
        // calls.
        // Also, JIT! ...unless this is a native method, in which case,
        // ...
        try {
            for (int i; (i = iStr.read()) != -1;)
                oStr.write(i);
        } catch (IOException e) {
        }

        closeStream(iStr);
        closeStream(oStr);
    }

    public boolean svgFileCopyCli(File p_from, File p_to) {
        // Copy the selected file to the sketchs data folder so Processing can actually
        // load it
        // without hackery with `PApplet.loadShape()`'s `XML` class logic:
        FileInputStream iStr = null;
        FileOutputStream oStr = null;

        try {
            oStr = new FileOutputStream(p_to);
        } catch (IOException e) {
            return false;
        }

        try {
            iStr = new FileInputStream(p_from);
        } catch (IOException e) {
            closeStream(oStr);
            return false;
        }

        // Is copying an array faster (at the expense of memory, of course!)?
        // 'cause I'm gunna use that if it *is* so...
        // ..and I feel that it IS.

        // Realization: two loops are better than one no matter the number of functions
        // calls.
        // Also, JIT! ...unless this is a native method, in which case,
        // ...

        try {
            for (int i; (i = iStr.read()) != -1;)
                oStr.write(i);
        } catch (IOException e) {
            return false;
        } finally {
            closeStream(iStr);
            closeStream(oStr);
        }

        return true;
    }

    public void resizeThenCenter(int p_x, int p_y) {
        window.setSize(p_x, p_y);
        centerWindow();
        setWindowsInPlace(); // I won't update the logic for that single line all the time :|
    }

    public void setWindowsInPlace() {
        surface.setLocation(0, 0);
        optionsForm.getWindow().setLocation(width, 0);
    }

    public void windowAtOrigin() {
        surface.setLocation(0, 0);
    }

    // Kinda' broken...
    public void centerWindow() {
        surface.setLocation((int) (displayWidth / 2 - cx), (int) (displayHeight / 2 - qy));
    }

    // To be used for cross-platform support.
    // void showInExplorer() {
    // }
    public void mousePressed() {
        if (svg != null)
            drawSvgToClearedBuffer(svg, render);
        println("User clicked! Refreshed buffer.");
    }

    public void keyPressed() {
        if (keyCode == 27)
            key = '\0';
    }

    public void dropEvent(DropEvent p_evt) {
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
            } catch (IOException e) {
            }

        svgFileCopy(realFile, dataFile);

        // synchronized(svg) {} // Causes the image to just... *NOT LOAD!* (..or
        // display! It causes lag, too...)
        svg = loadShape("svg.svg");

        exportName = realFile.getName();
        dataFile.delete();

        appState = AppState.DISPLAY;
        drawSvgToClearedBuffer(svg, render);
        optionsForm.getById("checkbox_style").setValue(Boolean.FALSE);

        surface.setSize(render.width, render.height);
        setWindowsInPlace();

        println("Loaded SVG file!");
    }

    // Sadly, this is broken.

    /**
     * How to use CLI Mode!: (Just... take all the data you need, please. Don't make
     * so many formats!)
     * 
     * 
     * Command format:
     * `SvgToImage <width> <height> <boolean-style> "<path-to-file>"
     * "<export-path>"`.
     * 
     * That's literally only one format. Both simple for me, and every user to
     * rememeber :)
     * 
     * Also, `SvgToImage -help` will print that out! :D
     * 
     * 
     * ...the old stuff:
     * 
     * DEPRECATED!: ~~`SvgToImage <side> "<path-to-file>"`,~~
     * DEPRECATED!: ~~`SvgToImage <boolean-style> <side> "<path-to-file>"`,~~
     * 
     * `SvgToImage <width> <height> "<path-to-file>"`,
     * `SvgToImage <boolean-style> <width> <height> "<path-to-file>"`,
     * 
     * `SvgToImage <side> "<path-to-file>" "<export-path>"`,
     * `SvgToImage <boolean-style> <side> "<path-to-file>" "<export-path>"`,
     * 
     * `SvgToImage <width> <height> "<path-to-file>" "<export-path>"`,
     * `SvgToImage <boolean-style> <width> <height> "<path-to-file>"
     * "<export-path>"`.
     * 
     * `<export-path>` should end with the extension of the user's choice.
     * If it starts with a `.`, and has the file extension written after (e.g.
     * `.png`),
     * then it is interpreted as the file extension, and the file is placed at the
     * default path.
     * :)
     */

    // ...yeah, used a *bit* of functional programming here or something.
    // The arguments passed to the sketch are not needed by the GUI, are they?
    // It's not stored globally, and I won't do this in a class and all.
    // Functional programming #FTW *here only at least! ..and most!:*

    public void cliMode() {
        String sketchArgsStr = System.getProperty("sun.java.command");
        String[] sketchArgs = split(sketchArgsStr, " ");

        int commandStart = getCommandStart(sketchArgs);

        // Thanks to all the functional programming,
        // this has started to look a lot like C/C++!

        // PS I finally understood the proper definition for a class while writing that:
        // Classes are interfaces (APIs) to get work done by.
        // Don't use them just for storing data. If you do, treat them TRULY like
        // `struct`s.
        // Use static methods to modify them, and all'a ("all of") that.

        boolean check = inCliMode(sketchArgs, commandStart); // Have this here for scalability.
        // An `if-else` repeating the following `println()` statement would do fine,
        // but scalability matters! This is a real application's code! HUSH!
        println("We're in", check ? "CLI" : "GUI", "mode!");

        if (check) {
            appState = AppState.CLI;
            cliImpl(sketchArgs, commandStart);
        }
    }

    public int getCommandStart(String[] p_args) {
        int commandStart;
        for (commandStart = 0; commandStart < p_args.length; commandStart++)
            if (p_args[commandStart].equals(SKETCH_NAME))
                break;

        println("Command reading starts after index:", commandStart);
        return commandStart;
    }

    // Better to pass the entire array here - what if this needs changes in the
    // future?
    // PS `getCommandStart()` exists solely to disallow doing the same calculation
    // more than once.
    // Future changes could force us to break the functional pattern...
    // Use OOP in that case! Classes to the rescue! Caching! *No more globals!*

    public boolean inCliMode(String[] p_args, int p_commandStart) {
        return p_commandStart + 1 != p_args.length; // Addition is just easier for machines.
        // ^^^ If it is *equal* to `p_args.length - 1`, it'd mean that there were no
        // more commands passed, indicating that the application was in GUI mode.
        // It certainly cannot be greater! Which thread added that extra element?!
    }

    public void cliImpl(String[] p_commands, int p_commandStart) {
        surface.setVisible(false); // Since this is a Java window, it *does* actually hide.
        // No idea what's wrong with OpenGL ones :|

        /*
         * `commandStart` LOL : width!
         * `commandStart + 1` : height!
         * `commandStart + 2` : style!
         * `commandStart + 3` : file!!!
         * `commandStart + 4` : path to export to!1!!11!!1!
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
                    } catch (NumberFormatException e) {
                        exit();
                    }

                    if (pw < 0)
                        exit();

                    break;

                case 3:
                    try {
                        ph = Integer.valueOf(cmd);
                    } catch (NumberFormatException e) {
                        exit();
                    }

                    if (ph < 0)
                        exit();

                    break;

                case 2:
                    String cmdLow = cmd.toLowerCase();
                    if (cmdLow.equals("true") || cmdLow.equals("false"))
                        style = Boolean.valueOf(cmd);
                    else
                        exit();
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
                exportFile.isDirectory() ? File.separator.concat(exportName) : ""));

        ui.showInfoDialog("Check ya' desktop :D");
        exit();
    }

    public String removeQuotesAndSlashes(String p_str) {
        return p_str.replaceAll("\"", " ").replaceAll("/", File.separator);
        // return p_str.substring(1, p_str.length() - 1); // BAD! >:(
    }

    public void draw() {
        if (optionsForm != null)
            if (optionsForm.isClosedByUser())
                exit();

        if (exportName == null)
            surface.setTitle("SVG To Image Tool");
        else
            surface.setTitle("SVG To Image Tool - ".concat(exportName));

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
            optionsWin.setSize(320, 400); // 420); // Add another `20` for `btn_save_to`.
            optionsWin.setLocation(new Point(
                    displayWidth / 2 + INIT_WIDTH / 2, displayHeight / 2 - INIT_HEIGHT / 2));
        }

        switch (appState) {
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
                } else
                    try {

                        // The image can be rendered now! All checks passed! :white_check_mark:

                        image(render, cx, cy);
                    } catch (RuntimeException e) {
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

    FormBuilder optionsFormBuild;
    Form optionsForm;
    JDialog optionsWin;

    public void createOptionsForm() {
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

                synchronized (render) {
                    if (render != null)
                        render.save(outFile.getAbsolutePath());
                }

                exec("explorer", dataFolder.getAbsolutePath());
            }
        }).setID("btn_save");

        /*
         * optionsFormBuild.addButton("Export to... and view.", new Runnable() {
         * File out;
         * public void run() {
         * out = ui.showFileOrDirectorySelection();
         * //out = ui.showDirectorySelectionFromPath(dataFolder.getAbsolutePath());
         * 
         * if (out == null)
         * return;
         * 
         * boolean selectionWasFile = false;
         * 
         * if (out.isDirectory()) {
         * println("Exporting to directory: ", out.getAbsolutePath(), " with name: ",
         * exportName);
         * out = new File(out.getAbsolutePath()
         * .concat(File.separator)
         * .concat(exportName.substring(0, exportName.lastIndexOf(".") + 1)) // Include
         * the `.`!
         * .concat(exportFormat));
         * 
         * if (out.exists())
         * for (int i = 1;; i++) {
         * if (!(out = new File(out.getAbsolutePath()
         * .concat(File.separator)
         * .concat(exportName.substring(0, exportName.lastIndexOf(".")))
         * .concat(" (" + i + ")")
         * .concat(".")
         * .concat(exportFormat))).exists())
         * break;
         * }
         * } else if (out.getName().equals(exportName) && out.exists()) {
         * selectionWasFile = true;
         * println("Exporting as a file:", exportName, exportFormat);
         * 
         * if (!ui.showConfirmDialog("Are you sure you want to overwrite `"
         * .concat(out.getPath()).concat("`?"), "Overwrite file?"))
         * return;
         * 
         * } else selectionWasFile = true;
         * 
         * synchronized(render) {
         * if (render != null)
         * if (selectionWasFile)
         * render.save(out.getAbsolutePath());
         * else
         * render.save(out.getAbsolutePath()
         * .concat(File.separator)
         * .concat(exportName.substring(0, exportName.lastIndexOf(".") + 1))
         * .concat(exportFormat));
         * }
         * 
         * if (selectionWasFile)
         * exec("explorer", out.getParent());
         * else
         * exec("explorer", out.getAbsolutePath());
         * }
         * }
         * ).setID("btn_save_to");
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
                        else
                            svg.enableStyle();

                        synchronized (render) {
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

                            // Do the rest of the error checks later so you do less checking when you return
                            // anyway :|
                            if (gotVal > MAX_DIM)
                                gotVal = MAX_DIM;

                            widthVal = gotVal;
                            println("Width change:", "`" + valStr + "`");
                        } catch (NumberFormatException e) {
                        }

                        if (widthVal < 1)
                            widthVal = DEFAULT_IMAGE_DIM;

                        // Yes. Keep the OTHER one in sync!:
                        optionsForm.getById("textarea_height").setValue(heightVal);

                        synchronized (render) {
                            render = createGraphics(widthVal, render.height);
                            drawSvgToClearedBuffer(svg, render);
                        }

                        previewTooLarge = widthVal > displayWidth || heightVal > displayHeight;
                        // widthVal * render.height >
                        // (displayWidth - displayWidth / 6) * (displayHeight - displayHeight / 6)

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

                            // Do the rest of the error checks later so you do less checking when you return
                            // anyway :|
                            if (gotVal > MAX_DIM)
                                gotVal = MAX_DIM;

                            heightVal = gotVal;
                            println("Height change:", "`" + valStr + "`");
                        } catch (NumberFormatException e) {
                        }

                        if (heightVal < 1)
                            heightVal = DEFAULT_IMAGE_DIM;

                        // Yes. Keep the OTHER one in sync!:
                        optionsForm.getById("textarea_width").setValue(widthVal);

                        synchronized (render) {
                            render = createGraphics(render.width, heightVal);
                            drawSvgToClearedBuffer(svg, render);
                        }

                        previewTooLarge = widthVal > displayWidth || heightVal > displayHeight;
                        // heightVal * render.width >
                        // (displayWidth - displayWidth / 6) * (displayHeight - displayHeight / 6);

                        if (!previewTooLarge)
                            surface.setSize(render.width + 10, heightVal + 10);
                        else
                            surface.setSize(DEFAULT_IMAGE_DIM, DEFAULT_IMAGE_DIM);
                        setWindowsInPlace();

                        break;

                    case "selection_format":
                        // Readable, READABLE!
                        exportFormat = p_ele.asString().toLowerCase();
                        println("Changed format to:", "`" + (exportFormat) + "`");
                        break;
                }
            }
        });

        // Was trying to remove the icon from here by calling
        // `optionsWin.setUndecorated(true);`
        // ...to prevent the user from closing the form.
        // I'll just close the application from now on.

        // JDialog optionsWin = optionsForm.getWindow();
        // `optionsWin.setDisplayable(false);`?
        // optionsWin.removeNotify();
        // optionsWin.addNotify();

        // Imagine the program not continuing to `draw()` O_O
        // if (optionsForm.isClosedByUser())
        // exit();
        // Oh wait, it isn't even present yet!
    }

}
