package com.dedalo.oprompter.modern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FOLDER_REQUEST = 2;
    private static final String PREFS_NAME = "oprompter_prefs";
    private static final String KEY_TREE_URI = "tree_uri";

    private ScrollView scrollView;
    private Button buttonAdvance;
    private EditText editTextPrompter;
    private TextView textViewTooltip;
    private TextView textViewPath;
    private View symbolPanel;
    private KeyListener savedKeyListener;
    private boolean editMode = false;
    private MenuItem editMenuItem;
    private String currentFileName = "";
    private String rawTextWithSymbols = "";
    private Uri treeUri = null;
    private Typeface musicTypeface = null;
    private boolean pendingSave = false;
    private boolean pendingLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = findViewById(R.id.scrollViewPrompter);
        buttonAdvance = findViewById(R.id.buttonAdvance);
        editTextPrompter = findViewById(R.id.editTextPrompter);
        textViewTooltip = findViewById(R.id.textViewTooltip);
        textViewPath = findViewById(R.id.textViewPath);
        symbolPanel = findViewById(R.id.symbolPanel);

        try {
            musicTypeface = ResourcesCompat.getFont(this, R.font.notomusic);
        } catch (Exception e) {
            musicTypeface = null;
        }

        savedKeyListener = editTextPrompter.getKeyListener();
        editTextPrompter.setKeyListener(null);

        editTextPrompter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyMusicFont(s);
            }
        });

        setupSymbolButtons();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUri = prefs.getString(KEY_TREE_URI, null);
        if (savedUri != null) {
            treeUri = Uri.parse(savedUri);
        }
        updatePathView();

        buttonAdvance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int scrollAmount = scrollView.getHeight();
                scrollView.smoothScrollBy(0, scrollAmount);
            }
        });

        buttonAdvance.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setTag(true);
                Toast.makeText(MainActivity.this, "Modo arrastre activado", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        buttonAdvance.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Boolean.TRUE.equals(v.getTag())) {
                            v.setX(event.getRawX() + dX);
                            v.setY(event.getRawY() + dY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setTag(false);
                        break;
                }
                return false;
            }
        });
    }

    private String getFolderDisplay() {
        if (treeUri == null) return null;
        try {
            String path = treeUri.getPath();
            if (path != null && path.startsWith("/tree/")) {
                path = path.substring(6);
            }
            if (path != null && path.startsWith("primary:")) {
                path = path.substring(8);
            }
            if (path == null || path.isEmpty()) {
                path = treeUri.toString();
            }
            return path;
        } catch (Exception e) {
            return treeUri.toString();
        }
    }

    private void updatePathView() {
        String display = getFolderDisplay();
        if (display == null) {
            textViewPath.setVisibility(View.GONE);
        } else {
            textViewPath.setText("Carpeta: " + display);
            textViewPath.setVisibility(View.VISIBLE);
        }
    }

    private static boolean isMusicCodePoint(int cp) {
        return (cp >= 0x2660 && cp <= 0x267F) || (cp >= 0x1D100 && cp <= 0x1D1FF);
    }

    private void applyMusicFont(Spannable text) {
        if (musicTypeface == null) return;
        MusicTypefaceSpan[] oldSpans = text.getSpans(0, text.length(), MusicTypefaceSpan.class);
        for (MusicTypefaceSpan old : oldSpans) {
            text.removeSpan(old);
        }
        int len = text.length();
        int i = 0;
        while (i < len) {
            int cp = Character.codePointAt(text, i);
            if (isMusicCodePoint(cp)) {
                int start = i;
                while (i < len) {
                    int c = Character.codePointAt(text, i);
                    if (!isMusicCodePoint(c)) break;
                    i += Character.charCount(c);
                }
                text.setSpan(new MusicTypefaceSpan(musicTypeface), start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                i += Character.charCount(cp);
            }
        }
    }

    private static class MusicTypefaceSpan extends TypefaceSpan {
        private final Typeface typeface;

        MusicTypefaceSpan(Typeface typeface) {
            super("notomusic");
            this.typeface = typeface;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            applyCustomTypeFace(ds, typeface);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            applyCustomTypeFace(paint, typeface);
        }

        private static void applyCustomTypeFace(TextPaint paint, Typeface tf) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            if (old == null) {
                oldStyle = 0;
            } else {
                oldStyle = old.getStyle();
            }
            int fake = oldStyle & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) paint.setFakeBoldText(true);
            if ((fake & Typeface.ITALIC) != 0) paint.setTextSkewX(-0.25f);
            paint.setTypeface(tf);
        }
    }

    private void setupSymbolButtons() {
        final int[] ids = {
                R.id.btnSharp, R.id.btnFlat, R.id.btnNatural,
                R.id.btnBar, R.id.btnRepeat, R.id.btnTimes,
                R.id.btnWhole, R.id.btnHalf, R.id.btnNegra, R.id.btnCorchea, R.id.btnSemicorchea,
                R.id.btnClef, R.id.btnMaj7, R.id.btnDim, R.id.btnHalfDim
        };
        final String[] symbols = {
                "♯", "♭", "♮",
                "|", "%", "x",
                "\uD834\uDD5D", "\uD834\uDD5E", "\uD834\uDD5F", "\uD834\uDD60", "\uD834\uDD61",
                "\uD834\uDD1E", "△", "°", "ø"
        };
        for (int i = 0; i < ids.length; i++) {
            final String symbol = symbols[i];
            findViewById(ids[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertAtCursor(symbol);
                }
            });
        }
    }

    private void insertAtCursor(String symbol) {
        int start = editTextPrompter.getSelectionStart();
        int end = editTextPrompter.getSelectionEnd();
        if (start < 0 || end < 0) {
            editTextPrompter.append(symbol);
            return;
        }
        editTextPrompter.getText().replace(Math.min(start, end), Math.max(start, end), symbol);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        editMenuItem = menu.findItem(R.id.action_edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            toggleEditMode();
            return true;
        } else if (id == R.id.action_pick_folder) {
            pickFolder();
            return true;
        } else if (id == R.id.action_load) {
            showLoadDialog();
            return true;
        } else if (id == R.id.action_save) {
            showSaveDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void pickFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        try {
            startActivityForResult(Intent.createChooser(intent, "Elegir carpeta de trabajo"), PICK_FOLDER_REQUEST);
        } catch (Exception e) {
            pendingSave = false;
            pendingLoad = false;
            Toast.makeText(this, "El sistema no permitió abrir el explorador", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FOLDER_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            treeUri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            } catch (Exception e) {
                // Si el proveedor no permite persistir, el permiso dura solo esta sesión
            }
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_TREE_URI, treeUri.toString())
                    .apply();
            updatePathView();
            Toast.makeText(this, "Carpeta de trabajo configurada", Toast.LENGTH_SHORT).show();

            if (pendingSave) {
                pendingSave = false;
                showSaveDialog();
            } else if (pendingLoad) {
                pendingLoad = false;
                showLoadDialog();
            }
        }
    }

    private ArrayList<String[]> listTextFiles() {
        ArrayList<String[]> result = new ArrayList<String[]>();
        if (treeUri == null) return result;
        try {
            String treeDocId = DocumentsContract.getTreeDocumentId(treeUri);
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId);
            Cursor cursor = getContentResolver().query(childrenUri, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
            }, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String docId = cursor.getString(0);
                    String name = cursor.getString(1);
                    if (name != null && name.toLowerCase().endsWith(".txt")) {
                        result.add(new String[]{name, docId});
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            result.clear();
        }
        Collections.sort(result, new Comparator<String[]>() {
            public int compare(String[] a, String[] b) {
                return a[0].compareToIgnoreCase(b[0]);
            }
        });
        return result;
    }

    private void showLoadDialog() {
        if (treeUri == null) {
            pendingLoad = true;
            Toast.makeText(this, "Primero elige una carpeta de trabajo", Toast.LENGTH_LONG).show();
            pickFolder();
            return;
        }
        final ArrayList<String[]> files = listTextFiles();
        if (files.isEmpty()) {
            Toast.makeText(this, "No hay archivos .txt en la carpeta elegida", Toast.LENGTH_SHORT).show();
            return;
        }
        final CharSequence[] items = new CharSequence[files.size()];
        for (int i = 0; i < files.size(); i++) items[i] = files.get(i)[0];
        new AlertDialog.Builder(this)
                .setTitle("Cargar archivo")
                .setMessage("Carpeta: " + getFolderDisplay())
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        loadFromDocument(files.get(which)[1], files.get(which)[0]);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadFromDocument(String docId, String displayName) {
        try {
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
            InputStream inputStream = getContentResolver().openInputStream(docUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
            reader.close();

            rawTextWithSymbols = content.toString();
            currentFileName = displayName.replace(".txt", "");

            SpannableString formattedText = MarkdownParser.parse(rawTextWithSymbols);
            editTextPrompter.setText(formattedText);
            updateTitle();

            scrollView.scrollTo(0, 0);
            Toast.makeText(this, "Archivo cargado: " + currentFileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar el archivo", Toast.LENGTH_LONG).show();
        }
    }

    private void showSaveDialog() {
        if (treeUri == null) {
            pendingSave = true;
            Toast.makeText(this, "Primero elige una carpeta de trabajo", Toast.LENGTH_LONG).show();
            pickFolder();
            return;
        }
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nombre del archivo");
        if (!currentFileName.isEmpty()) {
            input.setText(currentFileName);
        }
        new AlertDialog.Builder(this)
                .setTitle("Guardar archivo")
                .setMessage("Carpeta: " + getFolderDisplay())
                .setView(input)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String fileName = input.getText().toString().trim();
                        if (!fileName.isEmpty()) {
                            saveToFolder(fileName);
                        } else {
                            Toast.makeText(MainActivity.this, "Nombre vacío", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String findDocIdByName(String displayName) {
        ArrayList<String[]> files = listTextFiles();
        for (String[] f : files) {
            if (f[0].equals(displayName)) return f[1];
        }
        return null;
    }

    private void saveToFolder(String fileName) {
        if (treeUri == null) {
            pendingSave = true;
            Toast.makeText(this, "Primero elige una carpeta de trabajo", Toast.LENGTH_LONG).show();
            pickFolder();
            return;
        }
        try {
            String displayName = fileName + ".txt";
            String existingDocId = findDocIdByName(displayName);
            Uri docUri;
            if (existingDocId != null) {
                docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, existingDocId);
            } else {
                Uri treeDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));
                docUri = DocumentsContract.createDocument(getContentResolver(), treeDocUri, "text/plain", displayName);
            }
            if (docUri == null) {
                Toast.makeText(this, "No se pudo crear el archivo", Toast.LENGTH_LONG).show();
                return;
            }
            OutputStream out = getContentResolver().openOutputStream(docUri, "wt");
            out.write(rawTextWithSymbols.getBytes("UTF-8"));
            out.flush();
            out.close();

            currentFileName = fileName;
            updateTitle();
            Toast.makeText(this, "Guardado: " + displayName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleEditMode() {
        editMode = !editMode;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (editMode) {
            editTextPrompter.setText(rawTextWithSymbols);
            editTextPrompter.setKeyListener(savedKeyListener);
            editTextPrompter.requestFocus();
            editTextPrompter.setSelection(editTextPrompter.getText().length());
            imm.showSoftInput(editTextPrompter, InputMethodManager.SHOW_IMPLICIT);
            textViewTooltip.setVisibility(View.VISIBLE);
            symbolPanel.setVisibility(View.VISIBLE);
            if (editMenuItem != null) editMenuItem.setTitle("Terminar Edición");
            Toast.makeText(this, "Modo edición: usa * _ ~ + y el panel de símbolos", Toast.LENGTH_SHORT).show();
        } else {
            rawTextWithSymbols = editTextPrompter.getText().toString();
            SpannableString formattedText = MarkdownParser.parse(rawTextWithSymbols);
            editTextPrompter.setText(formattedText);
            editTextPrompter.setKeyListener(null);
            imm.hideSoftInputFromWindow(editTextPrompter.getWindowToken(), 0);
            textViewTooltip.setVisibility(View.GONE);
            symbolPanel.setVisibility(View.GONE);
            if (editMenuItem != null) editMenuItem.setTitle("Editar Texto");
            Toast.makeText(this, "Modo prompter activado", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTitle() {
        if (!currentFileName.isEmpty()) {
            setTitle("O'Prompter: " + currentFileName);
        } else {
            setTitle("O'Prompter");
        }
    }
}