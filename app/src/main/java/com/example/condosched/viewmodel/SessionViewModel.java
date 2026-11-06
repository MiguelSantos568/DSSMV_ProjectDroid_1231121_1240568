package com.example.condosched.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.condosched.domain.HomeSession;
import com.example.condosched.repository.SessionRepository;

public class SessionViewModel extends AndroidViewModel {
    private final SessionRepository repository;

    public SessionViewModel(Application application) {
        super(application);
        repository = SessionRepository.getInstance(application);
    }

    public LiveData<HomeSession> getSession() {
        return repository.getSession();
    }

    public HomeSession current() {
        return repository.current();
    }

    public void logout() {
        repository.clear();
    }

}
