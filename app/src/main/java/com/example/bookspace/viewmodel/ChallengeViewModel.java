package com.example.bookspace.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.bookspace.database.entity.ChallengeEntity;
import com.example.bookspace.database.entity.ReadingProgressEntity;
import com.example.bookspace.repository.ChallengeRepository;
import com.example.bookspace.repository.ProgressRepository;

import java.util.List;

public class ChallengeViewModel extends AndroidViewModel {
    private final ChallengeRepository challengeRepository;
    private final ProgressRepository progressRepository;
    private final LiveData<List<ChallengeEntity>> allChallenges;

    public ChallengeViewModel(@NonNull Application application) {
        super(application);
        challengeRepository = new ChallengeRepository(application);
        progressRepository = new ProgressRepository(application);
        allChallenges = challengeRepository.getAllChallenges();
    }

    public LiveData<List<ChallengeEntity>> getAllChallenges() {
        return allChallenges;
    }

    public void syncProgress() {
        List<ChallengeEntity> active = allChallenges.getValue();
        if (active == null) return;

        for (ChallengeEntity challenge : active) {
            if ("active".equals(challenge.status) && "pages".equals(challenge.challengeType)) {
                ReadingProgressEntity progress = progressRepository.getProgress(challenge.bookId);
                if (progress != null && progress.currentPage > challenge.currentValue) {
                    int increment = progress.currentPage - challenge.currentValue;
                    challengeRepository.updateProgress(challenge.id, increment);
                }
            }
        }
    }

    public void insert(ChallengeEntity challenge) {
        challengeRepository.insert(challenge);
    }
}
