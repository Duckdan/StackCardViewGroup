package study.yang.stackcardviewgroup;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private DefineStackAdapter defineStackAdapter;
    private DefineLeftLayoutManager layoutManager1;
    private List<String> datas;
    private DefineRightLayoutManager drlm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        RecyclerView rv1 = (RecyclerView) findViewById(R.id.rv_1);

        datas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            datas.add(String.valueOf(i));
        }
        DefineConfig defineConfig = new DefineConfig();
        defineConfig.initialStackCount = 3;
        defineConfig.space = 45;
        defineConfig.scaleRatio = 0.2f;
        layoutManager1 = new DefineLeftLayoutManager(this, defineConfig);
        drlm = new DefineRightLayoutManager(this, defineConfig);
        rv1.setLayoutManager(drlm);
        ArrayList<String> datas2 = new ArrayList<>();
        datas2.addAll(datas);
        rv1.setAdapter(new DefineStackAdapter(this, datas2));

        defineStackAdapter = new DefineStackAdapter(this, datas);
        rv.setLayoutManager(layoutManager1);
        rv.setAdapter(defineStackAdapter);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                if ((visibleItemCount > 0 && newState == RecyclerView.SCROLL_STATE_IDLE && (layoutManager1.findLastVisibleItemPosition()) >= totalItemCount - 1)) {
                    onLoadNextPage(recyclerView);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });


    }

    private void onLoadNextPage(RecyclerView recyclerView) {
        Log.e("onLoad", "加载到底部了，重新加载数据");
        int size = datas.size();
        int endSize = (size + 5 > 15) ? 15 : size + 5;
        for (int i = size; i < endSize; i++) {
            datas.add(String.valueOf(i));
        }
        defineStackAdapter.notifyDataSetChanged();

    }

}
