package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import com.googlecode.javacv.cpp.opencv_imgproc;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseInteractor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseUtil;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Photo;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Student;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.Labels;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.PersonRecognizer;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.ProcessaImagem;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

public class CadastroActivity extends AppCompatActivity {

    private LruCache<String, Bitmap> mMemoryCache;
    EditText name;
    EditText number;
    ProcessaImagem meuProcesso = new ProcessaImagem();

    public int capture=2;
    private boolean mReturningWithResult = false;
    private TextInputLayout inputLayoutName;
    private TextInputLayout inputLayoutNumber;
    PersonRecognizer fr;
    String Path="";
    String FacefileString ="";
    static final int MaxPeople = 200;
    Boolean saveFile;
    Labels labelsFile;
    public Toolbar toolbar;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("Opencv", "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //   System.loadLibrary("detection_based_tracker");

                   // Toast.makeText(getApplicationContext(),"", Toast.LENGTH_LONG).show();


                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {

        //ProcessaImagem meuProcesso = new ProcessaImagem();

        return mMemoryCache.get(key);
    }

    public ImageButton getButtonID(int requestCode)
    {
        ImageButton btn = new ImageButton(this);
        switch (requestCode){
            case 1:
                btn = (ImageButton) findViewById(R.id.button1);
                break;
            case 2:
                btn = (ImageButton) findViewById(R.id.button2);
                break;
            case 3:
                btn = (ImageButton) findViewById(R.id.button3);
                break;
            case 4:
                btn = (ImageButton) findViewById(R.id.button4);
                break;
            case 5:
                btn = (ImageButton) findViewById(R.id.button5);
                break;
            case 6:
                btn = (ImageButton) findViewById(R.id.button6);
                break;
            case 7:
                btn = (ImageButton) findViewById(R.id.button7);
                break;
            case 8:
                btn = (ImageButton) findViewById(R.id.button8);
                break;
            case 9:
                btn = (ImageButton) findViewById(R.id.button9);
                break;

        }
        return btn;

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mReturningWithResult) {
            // Commit your transactions here.
        }
        // Reset the boolean flag back to false for next time.
        mReturningWithResult = false;
    }



    public Bitmap readBitmap(int i, String flag) {
        Bitmap a = null;
        File f = Environment.getExternalStorageDirectory();
        File image = new File(f+"/EESC-Face/temp",Integer.toString(i)+flag+".jpg");
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        a = BitmapFactory.decodeFile(image.getAbsolutePath(), options);
        boolean deleted =image.delete();


        return a;

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //super.onActivityResult(requestCode, resultCode, data);
        //;

        for (int i = 0; i < 9; i++)
        {
            Bitmap temp = readBitmap(i,"B");
            if ((temp!=null) &&(temp.getWidth()>0))
            {
                ImageButton btn = getButtonID(i + 1);
                btn.setBackgroundResource(R.color.trans);
                btn.setImageBitmap(temp);
                btn.setClickable(false);
                if(i==8)
                {addBitmapToMemoryCache("face",temp);}
            }

            Bitmap dataFace = readBitmap(i,"A");
            if ((dataFace!=null) &&(dataFace.getWidth()>0))
            {
                addBitmapToMemoryCache(Integer.toString(i),dataFace);
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro4);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        Button btn_ok = (Button)findViewById(R.id.button10);
        ImageButton btn_one = (ImageButton)findViewById(R.id.button1);
        ImageButton btn_two = (ImageButton)findViewById(R.id.button2);
        ImageButton btn_three = (ImageButton)findViewById(R.id.button3);
        ImageButton btn_four = (ImageButton)findViewById(R.id.button4);
        ImageButton btn_five = (ImageButton)findViewById(R.id.button5);
        ImageButton btn_six = (ImageButton)findViewById(R.id.button6);
        ImageButton btn_seven = (ImageButton)findViewById(R.id.button7);
        ImageButton btn_eight = (ImageButton)findViewById(R.id.button8);
        ImageButton btn_nine = (ImageButton)findViewById(R.id.button9);
        name = (EditText)findViewById(R.id.namee);
        number = (EditText)findViewById(R.id.nUSP1);
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        inputLayoutName = (TextInputLayout) findViewById(R.id.name);
        inputLayoutNumber = (TextInputLayout) findViewById(R.id.nUSP);
        name.addTextChangedListener(new MyTextWatcher(name));
        number.addTextChangedListener(new MyTextWatcher(number));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Novo Aluno");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        Path=Environment.getExternalStorageDirectory()+"/EESC-FACE/faces/";
        FacefileString =Environment.getExternalStorageDirectory()+"/EESC-FACE/faces/Face-Recognizer.xml";
        File faceFile = new File(FacefileString);
        //mPath=Environment.getExternalStorageDirectory()+"/EE/faces/";
        fr=new PersonRecognizer(this,Path);

        if (faceFile.exists()) {
            fr.load2(FacefileString);
        }
        else
        {
            try {
                File f=new File (Path+"Face-Recognizer.xml");
                if (!f.isDirectory())
                {
                    //String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(Path);
                    myDir.mkdirs();
                }
                f.createNewFile();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e("error", e.getMessage() + " " + e.getCause());
                e.printStackTrace();
            }
        }

/*        labelsFile= new Labels(Path);
        labelsFile.Read();*/

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };




        btn_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 1);
            }

        });

        btn_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 2);


            }

        });

        btn_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 3);
            }

        });

        btn_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 4);
            }

        });

        btn_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 5);
            }

        });

        btn_six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 6);
            }

        });

        btn_seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 7);
            }

        });

        btn_eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 8);
            }

        });

        btn_nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CadastroActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);

                i.putExtra("flag_qdt_foto",capture);
                startActivityForResult(i, 9);
            }

        });

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // name.setText("");

            }

        });

        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //number.setText("");

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_course, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.done_newcourse) {
            saveStudent();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void saveStudent()
    {
        if (valida())
        {
            Student student = new Student();
            student.setName(name.getText().toString());
            student.setStudentCode(Long.decode(number.getText().toString()));
            student.setPhotoList(new ArrayList<Photo>());
            DatabaseInteractor.saveStudent(CadastroActivity.this, student);

            int[] labels = new int[9];
            MatVector matVector = new MatVector(9);
            Size size = new Size(130, 150);

            for (int i = 0; i < 9; i++) {
                Bitmap faceB = getBitmapFromMemCache(Integer.toString(i));
                Bitmap faceE = getBitmapFromMemCache("face");
                Mat faceEE = ProcessaImagem.Bitmap_to_Mat(faceE);
                Imgproc.resize(faceEE, faceEE, size);

                Bitmap faceC = ProcessaImagem.MattoBitmap(faceEE);

                if ((faceB != null) && (faceB.getWidth() > 0)) {
                    saveFile=true;
                    Mat faceM = ProcessaImagem.Bitmap_to_Mat(faceB);
                    Imgproc.resize(faceM, faceM, size);
                    IplImage IplFace;
                    MatToIplImage(faceM,faceM.width(),faceM.height());
                    IplFace=MatToIplImage(faceM,faceM.width(),faceM.height());
                    matVector.put(i,IplFace);
                    labels[i]= Integer.parseInt(number.getText().toString());//atencao!! escolher add ID aluno
                    //salva foto no aluno //**************************************//*
                    Photo photo = new Photo();
                    photo.setImage(DatabaseUtil.bitmapToString(faceC));
                    photo.setStudentId(DatabaseInteractor.getStudentByCode(CadastroActivity.this, student.getStudentCode()).getId());
                    DatabaseInteractor.savePhoto(CadastroActivity.this, photo);                            //fr.add(faceM,name.getText().toString());
                } else {
                    alertBox();
                    saveFile=false;
                }

            }
            if(saveFile)
            {
                final MatVector m = matVector;
                final int[] lab = labels;
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fr.update(m,lab,name.getText().toString(), FacefileString);
                        sucess();
                        finish();
                    }
                });
                t.start();
                //Intent x = new Intent(Cadastro4.this, Cadastro3.class);
                //startActivity(x);

            }


        }


    }


    public Boolean validateImage()
    {
        Boolean ImageCheck = false;
        for (int i = 0; i < 9; i++) {
            Bitmap faceB = getBitmapFromMemCache(Integer.toString(i));
            if ((faceB != null) && (faceB.getWidth() > 0))
            {ImageCheck= true;}
            else
            {
              ImageCheck=false;
                alertBox();
                break;
            }
        }
        return ImageCheck;
    }
    public void alertBox()
    {

        new AlertDialog.Builder(CadastroActivity.this)
                //.setTitle("Adicione uma imagem")
                .setMessage("Adicione uma imagem")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })

                .show();

    }

    public void sucess()
    {
        this.runOnUiThread(new Runnable() {
            public void run() {
                int duration = Toast.LENGTH_SHORT;
                Context context = getApplicationContext();
                CharSequence sucess = "Cadastro salvo com sucesso !";
                Toast toast = Toast.makeText(context, sucess, duration);
                toast.show();
            }
        });



    }

    opencv_core.IplImage MatToIplImage(Mat m,int width,int heigth)
    {


        Bitmap bmp=Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


        Utils.matToBitmap(m, bmp);
        return BitmapToIplImage(bmp,width, heigth);

    }

    opencv_core.IplImage BitmapToIplImage(Bitmap bmp, int width, int height) {

        if ((width != -1) || (height != -1)) {
            Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, false);
            bmp = bmp2;
        }

        opencv_core.IplImage image = opencv_core.IplImage.create(bmp.getWidth(), bmp.getHeight(),
                IPL_DEPTH_8U, 4);

        bmp.copyPixelsToBuffer(image.getByteBuffer());

        opencv_core.IplImage grayImg = opencv_core.IplImage.create(image.width(), image.height(),
                IPL_DEPTH_8U, 1);

        cvCvtColor(image, grayImg, opencv_imgproc.CV_BGR2GRAY);

        return grayImg;
    }


    private Boolean valida()
    {
        boolean a = validateName();
        boolean b = validateNumber();
        boolean c = validateImage();
        if(!a){return false;}
        else if(!b){return false;}
        else if (!c){return false;}
        else {return true;}
    }

    private boolean validateName() {
        if (name.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(name);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    public Boolean falseNumber(int i){
        String numb = number.getText().toString();
        boolean numInvalido=false;
        boolean numEmpty=false;
        try {
            int num = Integer.parseInt(numb);
            if ((num<1000000)||(num>10000000))
            { numInvalido =true;
            }

        } catch (NumberFormatException e) {
            numEmpty=true;
        }
        if (i==1)
            return numEmpty;
        else
            return numInvalido;

    }
    private boolean validateNumber() {

            if (number.getText().toString().trim().isEmpty()) {
                inputLayoutNumber.setError(getString(R.string.err_msg_num_empty));
                requestFocus(number);
                return false;
            } else {
                inputLayoutName.setErrorEnabled(false);
            }

            return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()) {
                case R.id.name:
                    validateName();
                    break;
                case R.id.nUSP:
                    validateNumber();
                    break;

            }

        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()) {
                case R.id.name:
                    validateName();
                    break;
                case R.id.nUSP:
                    validateNumber();
                    break;

            }


        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.name:
                    validateName();
                    break;
                case R.id.nUSP:
                    validateNumber();
                    break;

            }
        }
    }




}
