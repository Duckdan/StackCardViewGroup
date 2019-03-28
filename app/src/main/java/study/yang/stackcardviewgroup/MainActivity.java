package study.yang.stackcardviewgroup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CardView cv_1 = (CardView) findViewById(R.id.cv_1);
        CardView cv_2 = (CardView) findViewById(R.id.cv_2);
        CardView cv_4 = (CardView) findViewById(R.id.cv_4);
        CardView cv_5 = (CardView) findViewById(R.id.cv_5);
        CardView cv_6 = (CardView) findViewById(R.id.cv_6);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        cv_1.measure(0, 0);
        cv_1.setPivotX(900);
        cv_1.setPivotY(cv_1.getMeasuredHeight() * 1.0f / 2);
        float scaleX = 900 * 1.0f / displayMetrics.widthPixels;
        cv_1.setScaleX(0.6f);
        cv_1.setScaleY(0.6f);

        cv_4.setPivotX(900);
        cv_4.setPivotY(cv_1.getMeasuredHeight() * 1.0f / 2);
        cv_4.setScaleX(0.6f);
        cv_4.setScaleY(0.6f);

        cv_5.setPivotX(900);
        cv_5.setPivotY(cv_1.getMeasuredHeight() * 1.0f / 2);
        cv_5.setScaleX(0.8f);
        cv_5.setScaleY(0.8f);
    }
}
