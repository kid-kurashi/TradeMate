package ru.dwdm.trademate;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.HashMap;

import ru.dwdm.trademate.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private WritingFragment arFragment;
    private ModelRenderable modelRenderable;

    private MainActivityViewModel viewModel;
    private ActivityMainBinding binding;
    private String[] arrayPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final int PERMISSIONS_REQUEST = 200;
    private ArrayList<AnchorNode> anchorNodes = new ArrayList<>();

    private HashMap<Integer, Integer> modeslMap = new HashMap<>();
    private static final int SELECT_MODEL_REQUEST_CODE = 311;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        modeslMap.put(R.drawable.vending_machine, R.raw.model);
        modeslMap.put(R.drawable.knuckles, R.raw.ugandan_knuckles);
        modeslMap.put(R.drawable.poss, R.raw.possdd);
        modeslMap.put(R.drawable.wheelchair, R.raw.wheelchair);
        modeslMap.put(R.drawable.voda, R.raw.voda);


        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        getLifecycle().addObserver(viewModel);

        arFragment = (WritingFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.setCallback(new WritingFragment.WriteCallback() {
            @Override
            public void onWriteStart() {
                binding.savingProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onWriteEnd() {
                binding.savingProgress.setVisibility(View.GONE);
            }
        });

        setDefaultModelRenderable();

        setOnTapArPlaneListener();


        binding.screenshotButton.setOnClickListener(v -> {
            if (!checkPermissions()) {
                requestPermissions(arrayPermissions, PERMISSIONS_REQUEST);
            } else {
                arFragment.takePhoto();
            }
        });
        binding.more.setOnClickListener(this::showMenu);
        binding.back.setOnClickListener(v -> onBackPressed());


        viewModel.gridVisible.observe(this, toggle -> arFragment.getArSceneView().getPlaneRenderer().setEnabled(toggle));

    }

    private void setSelectedModelRenderable(int modelId) {
        ModelRenderable.builder()
                .setSource(this, modeslMap.get(modelId))
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load model renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    private void setDefaultModelRenderable() {
        ModelRenderable.builder()
                .setSource(this, R.raw.model)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load model renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    private void setOnTapArPlaneListener() {
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (modelRenderable == null) {
                        return;
                    }

                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNodes.add(anchorNode);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable model and add it to the anchor.
                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                    transformableNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 180f));
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(modelRenderable);
                    transformableNode.select();
                });
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.main_menu);
        popup.show();
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    private boolean checkPermissions() {
        for (String permission : arrayPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void toggleGrid() {
        viewModel.toggleGridVisible();
    }

    private void clearAll() {
        for (AnchorNode anchorNode : anchorNodes) {
            arFragment.getArSceneView().getScene().removeChild(anchorNode);
            anchorNode.getAnchor().detach();
        }
        anchorNodes.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permissions denied",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                arFragment.takePhoto();
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.clear_all:
                clearAll();
                return true;
            case R.id.toggle_grid:
                toggleGrid();
                return true;
            case R.id.select_model:
                startActivityForResult(new Intent(this, SelectModelActivity.class), SELECT_MODEL_REQUEST_CODE);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SELECT_MODEL_REQUEST_CODE && resultCode == RESULT_OK) {
            setSelectedModelRenderable(data.getIntExtra(SelectModelActivity.KEY_MODEL, R.drawable.vending_machine));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}