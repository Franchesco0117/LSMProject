package com.francisco.lsmproyect;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SignLanguageClass {
    // interpreter es usado para cargar el modelo y predecir
    private Interpreter interpreter;
    // Crear otro interpreter para el modelo Sign_language_model
    private Interpreter interpreter2;

    // Almacenar todas las etiquetas(labelmap) en este arreglo
    private List<String> labelList;
    private int INPUT_SIZE;
    private int PIXELS_SIZE = 3; // Para RGB
    private int IMAGE_MEAN = 0;
    private float IMAGE_STD = 255.0f;

    // gpuDelegate usado para inicializar la GPU en la App
    private GpuDelegate gpuDelegate;
    private int height = 0;
    private int width = 0;
    private int ClassificationInputSize = 0;
    private String finalText = "";
    private String currentText = "";
    private TextToSpeech tts;

    SignLanguageClass (Context context, Button btnClear, Button btnAdd, TextView tvChange, Button btnSpeak,
                       AssetManager assetManager, String modelPath, String labelPath, int inputSize, String classificationModel,
                       int classificationInput) throws IOException {
        INPUT_SIZE = inputSize;
        ClassificationInputSize = classificationInput;

        // options usado para definir la GPU, CPU o numero de threads
        Interpreter.Options options = new Interpreter.Options();
        // gpuDelegate = new GpuDelegate();
        // options.addDelegate(gpuDelegate);
        options.setNumThreads(4); // Poner la cantidad de hilos segun el celular (!!!)

        // Cargando modelo
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);

        // Cargar labelmap
        labelList = loadLabelList(assetManager, labelPath);

        Interpreter.Options options2 = new Interpreter.Options();
        options2.setNumThreads(2);
        interpreter2 = new Interpreter(loadModelFile(assetManager, classificationModel), options2);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalText = "";

                tvChange.setText(finalText);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finalText = finalText + currentText;
                tvChange.setText(finalText);
            }
        });

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                if (i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tts.speak(finalText, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // fileDescriptor usado para obtener la descripcion del archivo
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        // Para capturar el label
        List<String> labelList = new ArrayList<>();

        // Crear un nuevo lector
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;

        // Bucle para recorrer cada linea y capturarlo en labelList
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }

        reader.close();

        return labelList;
    }

    public Mat recognizeImage(Mat matImage) {
        // Rotar la imagen original por 90 grados para tener modo retrato
        Mat rotatedMatImage = new Mat();

        Mat a = matImage.t();
        Core.flip(a, rotatedMatImage, 1);
        a.release();

        // Si no haces sete proceso, obtendras menos predicciones correctas, menos numero de objectos
        // Ahora convertir a Bitmap
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(rotatedMatImage.cols(), rotatedMatImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotatedMatImage, bitmap);

        // Definir altura y ancho
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        // Escalar el Bitmap a Input size del modelo
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        // Convertir bitmap a Bytebuffer como modelo Input deberia de estar ahi
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);

        // Definiendo el output
        // 10: top 10 objetos detectados
        // 4: Condenadas de la imagen
        // float[][][] result = new float[1][10][4]; no se usa por ahora je
        Object[] input = new Object[1];
        input[0] = byteBuffer;

        // TreeMap: Arreglo de tres (boxes, score, classes)
        Map<Integer, Object> outputMap = new TreeMap<>();

        // 10: top 10 objetos detectados
        // 4: Condenadas de la imagen
        float [][][] boxes = new float[1][10][4];

        // Captura puntos de 10 objectos
        float [][] scores = new float[1][10];

        // Captura la clase del objecto
        float [][] classes = new float[1][10];

        // Añadirlo al objectMap;
        outputMap.put(0, boxes);
        outputMap.put(1, classes);
        outputMap.put(2, scores);

        // Ahora predecir
        interpreter.runForMultipleInputsOutputs(input, outputMap);

        Object value = outputMap.get(0);
        Object objectClass = outputMap.get(1);
        Object score = outputMap.get(2);

        // Recorrer cada objecto - Output solo tiene 10 boxes
        for (int i = 0; i < 10; i++){
            float classValue = (float) Array.get(Array.get(objectClass, 0), i);
            float scoreValue = (float) Array.get(Array.get(score, 0), i);

            // Definir entrada para la puntuación
            if (scoreValue > 0.5) {
                Object box1 = Array.get(Array.get(value, 0), i);

                // Lo estamos multiplicando con el tamaño orignal y la altura del frame
                float y1 = (float) Array.get(box1, 0)*height;
                float x1 = (float) Array.get(box1, 1)*width;
                float y2 = (float) Array.get(box1, 2)*height;
                float x2 = (float) Array.get(box1, 3)*width;

                //Establecer límite límite
                if (y1 < 0) {
                    y1 = 0;
                }

                if (x1 < 0) {
                    x1 = 0;
                }

                if (x2 > width) {
                    x2 = width;
                }

                if (y2 > height) {
                    y2 = height;
                }

                // Ahora poner el alto(height) y ancho(width) de la caja
                float w1 = x2 - x1;
                float h1 = y2 - y1;
                // (x1, y1) punto inicial de la mano
                // (x2, y2) punto final de la mano
                // Recortar imagen de mano del marco original
                Rect croppedRoi = new Rect((int)x1, (int)y1, (int)w1, (int)h1);
                Mat cropped = new Mat(rotatedMatImage, croppedRoi).clone();

                //Ahora convertir el Mat recortado a Bitmap
                Bitmap bitmap1 = null;
                bitmap1 = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(cropped, bitmap1);

                // Cambiar tamaño de Bitmap a Classification input size = 96;
                Bitmap scaleBitmap1 = Bitmap.createScaledBitmap
                        (bitmap1, ClassificationInputSize, ClassificationInputSize, false);
                // Convertir scaleBitmap1 a byte buffer
                ByteBuffer byteBuffer1 = convertBitmapToByteBuffer1(scaleBitmap1);

                //Crear un arreglo para la salida de interpreter2
                float[][] outputClassValue = new float[1][1];

                // Predecir salida para byteBuffer1
                interpreter2.run(byteBuffer1, outputClassValue);

                // OPCIONAL: Para ver los valores de outputClassValue
                Log.d("SignLanguageClass", "outputClassValue:" + outputClassValue[0][0] + "  " + getAlphabets(outputClassValue[0][0]));

                // Converir outputClassValue a Alfabeto
                String signVal = getAlphabets(outputClassValue[0][0]);

                currentText = signVal;

                // Dibujar rectangulo en el frame original  - Punto inicial box   - Punto final box       - color de box     - grueso
                Imgproc.rectangle(rotatedMatImage, new Point(x1, y1), new Point(x2, y2), new Scalar(255, 155, 155), 2);

                // Escribir texto en el frame
                //              - input / output   - text                  - Punto inicial          - Tamaño texto                         - Color de texto
                Imgproc.putText(rotatedMatImage, "" + signVal, new Point(x1+10, y1+40), 2, 1.5,  new Scalar(255, 255, 255, 255), 2);

            }
        }

        // Antes de return, rotar la imagen otra vez por -90 grados
        Mat b = rotatedMatImage.t();
        Core.flip(b, matImage, 0);
        b.release();

        return matImage;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        int quant = 1;
        int sizeImages = INPUT_SIZE;

        // Algunos modelos Input deberian de quant = 0; para algunos otros quant = 1;
        if (quant == 0) {
            byteBuffer = ByteBuffer.allocateDirect(1*sizeImages*sizeImages*3);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4*1*sizeImages*sizeImages*3);
        }

        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[sizeImages*sizeImages];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        for (int i = 0; i < sizeImages; ++i) {
            for (int j = 0; j < sizeImages; ++j) {
                final int val = intValues[pixel++];
                if (quant == 0) {
                    byteBuffer.put((byte) ((val >> 16 ) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8 ) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    byteBuffer.putFloat((((val >> 16) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val >> 8) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val) & 0xFF))/255.0f);
                }
            }
        }

        return byteBuffer;
    }

    private ByteBuffer convertBitmapToByteBuffer1(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        int quant = 1;
        //Cambiar el input size
        int sizeImages = ClassificationInputSize;

        // Algunos modelos Input deberian de quant = 0; para algunos otros quant = 1;
        if (quant == 0) {
            byteBuffer = ByteBuffer.allocateDirect(1*sizeImages*sizeImages*3);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4*1*sizeImages*sizeImages*3);
        }

        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[sizeImages*sizeImages];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        for (int i = 0; i < sizeImages; ++i) {
            for (int j = 0; j < sizeImages; ++j) {
                final int val = intValues[pixel++];
                if (quant == 0) {
                    byteBuffer.put((byte) ((val >> 16 ) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8 ) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    byteBuffer.putFloat((((val >> 16) & 0xFF)));
                    byteBuffer.putFloat((((val >> 8) & 0xFF)));
                    byteBuffer.putFloat((((val) & 0xFF)));
                }
            }
        }

        return byteBuffer;
    }

    private String getAlphabets(float signV) {
        String val = "";

        // A, B, C, D, E, F, I, L, O, R, T, V, W

        if (signV >= -0.5 & signV < 0.5) {
            val = "A";
        } else if (signV >= 0.5 & signV < 1.5) {
            val = "B";
        } else if (signV >= 1.5 & signV < 2.5) {
            val = "C";
        } else if (signV >= 2.5 & signV < 3.5) {
            val = "D";
        } else if (signV >= 3.5 & signV < 4.5) {
            val = "E";
        } else if (signV >= 4.5 & signV < 5.5) {
            val = "F";
        } else if (signV >= 5.5 & signV < 6.5) {
            val = "G";
        } else if (signV >= 6.5 & signV < 7.5) {
            val = "H";
        } else if (signV >= 7.5 & signV < 8.5) {
            val = "I";
        } else if (signV >= 8.5 & signV < 9.5) {
            val = "J";
        } else if (signV >= 9.5 & signV < 10.5) {
            val = "K";
        } else if (signV >= 10.5 & signV < 11.5) {
            val = "L";
        } else if (signV >= 11.5 & signV < 12.5) {
            val = "M";
        } else if (signV >= 12.5 & signV < 13.5) {
            val = "N";
        } else if (signV >= 13.5 & signV < 14.5) {
            val = "O";
        } else if (signV >= 14.5 & signV < 15.5) {
            val = "P";
        } else if (signV >= 15.5 & signV < 16.5) {
            val = "Q";
        } else if (signV >= 16.5 & signV < 17.5) {
            val = "R";
        } else if (signV >= 17.5 & signV < 18.5) {
            val = "S";
        } else if (signV >= 18.5 & signV < 19.5) {
            val = "T";
        } else if (signV >= 19.5 & signV < 20.5) {
            val = "U";
        } else if (signV >= 20.5 & signV < 21.5) {
            val = "V";
        } else if (signV >= 21.5 & signV < 22.5) {
            val = "W";
        } else if (signV >= 22.5 & signV < 23.5) {
            val = "X";
        } else if (signV >= 23.5 & signV < 24.5){
            val = "Y";
        } else {
            val = "Z";
        }

        return val;
    }

}
