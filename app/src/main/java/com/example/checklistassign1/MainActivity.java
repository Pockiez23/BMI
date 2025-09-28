package com.example.checklistassign1;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText editWeight, editHeight;
    private TextView textResult;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // เชื่อมต่อ UI components
        editWeight = findViewById(R.id.editWeight);
        editHeight = findViewById(R.id.editHeight);
        textResult = findViewById(R.id.textResult);
        btnCalculate = findViewById(R.id.btnCalculate);

        // ตั้งค่า InputFilter สำหรับจำกัดทศนิยม 2 หลัก
        editWeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 2)});
        editHeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 2)});

        // เมื่อกดปุ่มคำนวณ
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBMI();
            }
        });
    }

    private void calculateBMI() {
        try {
            String weightStr = editWeight.getText().toString().trim();
            String heightStr = editHeight.getText().toString().trim();

            if (weightStr.isEmpty() || heightStr.isEmpty()) {
                textResult.setText(getString(R.string.error_empty_fields));
                return;
            }

            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);

            if (weight <= 0 || height <= 0) {
                textResult.setText(getString(R.string.error_invalid_values));
                return;
            }

            if (height > 3) {
                height = height / 100.0;
            }

            double bmi = weight / (height * height);

            DecimalFormat formatter = new DecimalFormat("#.##");

            // Category หลัก
            String category;
            int colorRes;

            if (bmi < 18.5) {
                category = getString(R.string.category_underweight);
                colorRes = R.color.bmi_underweight;
            } else if (bmi >= 18.5 && bmi < 23.0) {
                category = getString(R.string.category_normal);
                colorRes = R.color.bmi_normal;
            } else if (bmi >= 23.0 && bmi < 25.0) {
                category = getString(R.string.category_overweight);
                colorRes = R.color.bmi_overweight;
            } else if (bmi >= 25.0 && bmi < 30.0) {
                category = getString(R.string.category_obese);
                colorRes = R.color.bmi_obese;
            } else {
                category = getString(R.string.category_severely_obese);
                colorRes = R.color.bmi_severe;
            }

            // -------------------- ใช้ diseases จาก arrays.xml --------------------
            String[] diseases = getResources().getStringArray(R.array.diseases_array);
            double[] risks = new double[diseases.length];

            for (int i = 0; i < diseases.length; i++) {
                switch (i) {
                    case 0: risks[i] = (bmi > 25) ? bmi * 1.2 : bmi * 0.5; break; // หัวใจ
                    case 1: risks[i] = (bmi > 23) ? bmi * 1.1 : bmi * 0.4; break; // เบาหวาน
                    case 2: risks[i] = (bmi > 25) ? bmi * 1.3 : bmi * 0.3; break; // ความดัน
                    case 3: risks[i] = (bmi > 23) ? bmi * 1.0 : bmi * 0.5; break; // ไขมัน
                    case 4: risks[i] = (bmi < 18.5) ? 20 : 5; break; // กระดูกพรุน
                }
            }

            // จัดเรียง top 3
            Integer[] idx = {0, 1, 2, 3, 4};
            java.util.Arrays.sort(idx, (a, b) -> Double.compare(risks[b], risks[a]));

            StringBuilder diseaseResult = new StringBuilder();
            diseaseResult.append("Top 3 :\n");
            for (int i = 0; i < 3; i++) {
                diseaseResult.append((i + 1)).append(". ")
                        .append(diseases[idx[i]])
                        .append(" (point: ").append(String.format("%.1f", risks[idx[i]])).append(")\n");
            }
            // -------------------------------------------------------------------

            // แสดงผลรวม
            String result = "ค่า BMI = " + formatter.format(bmi) + " (" + category + ")\n\n"
                    + diseaseResult.toString();
            textResult.setText(result);
            textResult.setTextColor(getResources().getColor(colorRes));

        } catch (NumberFormatException e) {
            textResult.setText(getString(R.string.error_invalid_number));
        }
    }


    // Inner class สำหรับ InputFilter
    public class DecimalDigitsInputFilter implements InputFilter {
        private int digitsBeforeZero;
        private int digitsAfterZero;

        public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            this.digitsBeforeZero = digitsBeforeZero;
            this.digitsAfterZero = digitsAfterZero;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            // ถ้าเป็น string ว่าง หรือ เป็นการลบ ให้ผ่านได้
            if (source == null || source.length() == 0) {
                return null;
            }

            // สร้าง text ใหม่หลังจากแก้ไข
            StringBuilder newText = new StringBuilder(dest);
            newText.replace(dstart, dend, source.subSequence(start, end).toString());
            String newString = newText.toString();

            // ถ้าเป็น string ว่าง ให้ผ่าน
            if (newString.isEmpty()) {
                return null;
            }

            // ตรวจสอบว่าเป็นตัวเลขและจุดทศนิยมเท่านั้น
            if (!newString.matches("[0-9.]*")) {
                return "";
            }

            // ตรวจสอบจำนวนจุดทศนิยม
            String[] parts = newString.split("\\.");
            if (parts.length > 2) {
                return ""; // มีจุดมากกว่า 1 จุด
            }

            // ตรวจสอบจำนวนหลักก่อนจุดทศนิยม
            if (parts[0].length() > digitsBeforeZero) {
                return "";
            }

            // ตรวจสอบจำนวนหลักหลังจุดทศนิยม
            if (parts.length == 2 && parts[1].length() > digitsAfterZero) {
                return "";
            }

            return null; // ยอมรับการป้อน
        }
    }
}