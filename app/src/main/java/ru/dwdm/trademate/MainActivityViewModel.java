package ru.dwdm.trademate;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

public class MainActivityViewModel extends AndroidViewModel implements LifecycleObserver {

    public MutableLiveData<Boolean> gridVisible = new MutableLiveData<>();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void init(){
        gridVisible.postValue(true);

    }

    public boolean isGridVisible() {
        return gridVisible.getValue();
    }

    public void toggleGridVisible(){
        gridVisible.postValue(!gridVisible.getValue());
    }
}
