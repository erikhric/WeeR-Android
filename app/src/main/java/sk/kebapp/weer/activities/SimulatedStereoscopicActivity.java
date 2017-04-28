package sk.kebapp.weer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SimulatedStereoscopicActivity extends StereoActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews(false);
    }

    public static void intentTo(Context context, String videoPath) {
        Intent intent = new Intent(context, SimulatedStereoscopicActivity.class);
        intent.putExtra("videoPath", videoPath);

        context.startActivity(intent);
    }
}
