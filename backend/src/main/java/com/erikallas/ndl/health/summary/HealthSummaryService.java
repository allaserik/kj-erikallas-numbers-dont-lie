package com.erikallas.ndl.health.summary;

import com.erikallas.ndl.health.weight.WeightEntryEntity;
import com.erikallas.ndl.health.weight.WeightEntryRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HealthSummaryService {

    private final WeightEntryRepository weightRepo;

    public HealthSummaryService(WeightEntryRepository weightRepo) {
        this.weightRepo = weightRepo;
    }

    public double bmi(int heightCm, double weightKg) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    public Double weightDelta7d(List<WeightEntryEntity> entries) {
        if (entries.size() < 2)
            return null;

        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(7);

        WeightEntryEntity newest = entries.get(0);
        WeightEntryEntity oldest = null;

        for (WeightEntryEntity e : entries) {
            if (e.getMeasuredAt().isBefore(cutoff)) {
                oldest = e;
                break;
            }
        }

        return oldest != null
                ? newest.getWeightKg() - oldest.getWeightKg()
                : null;
    }
}
