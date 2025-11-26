package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.models.Section;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.repositories.HomeRepository;

public class HomeViewModel extends AndroidViewModel {
    private final HomeRepository repo;

    // giữ LiveData cho từng loại
    private final LiveData<List<Tour>> toursBac;
    private final LiveData<List<Tour>> toursTrung;
    private final LiveData<List<Tour>> toursNam;
    private final LiveData<List<Tour>> hotTours;
    private final LiveData<List<Tour>> discountTours;

    private final MediatorLiveData<List<Section>> sectionsLive = new MediatorLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext()
                .getString(vn.androidhaui.foxtrip.R.string.base_url);



        repo = new HomeRepository(application.getApplicationContext(), baseUrl);

        // chỉ gọi 1 lần
        toursBac = repo.getToursBac();
        toursTrung = repo.getToursTrung();
        toursNam = repo.getToursNam();
        hotTours = repo.getHotTours();
        discountTours = repo.getDiscountTours();

        loadAllSections();
    }

    private void loadAllSections() {
        sectionsLive.addSource(toursBac, t -> updateSections());
        sectionsLive.addSource(toursTrung, t -> updateSections());
        sectionsLive.addSource(toursNam, t -> updateSections());
        sectionsLive.addSource(hotTours, t -> updateSections());
        sectionsLive.addSource(discountTours, t -> updateSections());
    }

    private void updateSections() {
        List<Section> sections = new ArrayList<>();
        if (toursBac.getValue() != null && !toursBac.getValue().isEmpty())
            sections.add(new Section("Miền Bắc", toursBac.getValue()));
        if (toursTrung.getValue() != null && !toursTrung.getValue().isEmpty())
            sections.add(new Section("Miền Trung", toursTrung.getValue()));
        if (toursNam.getValue() != null && !toursNam.getValue().isEmpty())
            sections.add(new Section("Miền Nam", toursNam.getValue()));
        if (hotTours.getValue() != null && !hotTours.getValue().isEmpty())
            sections.add(new Section("Tour bán chạy", hotTours.getValue()));
        if (discountTours.getValue() != null && !discountTours.getValue().isEmpty())
            sections.add(new Section("Tour giảm giá", discountTours.getValue()));

        android.util.Log.d("DEBUG_SECTIONS", "Sections size: " + sections.size());
        for (Section s : sections) {
            android.util.Log.d("DEBUG_SECTIONS", s.getTitle() + " -> " + (s.getTours() == null ? 0 : s.getTours().size()));
        }

        sectionsLive.postValue(sections);
    }

    public LiveData<List<Section>> getSectionsLive() { return sectionsLive; }
    public LiveData<List<Tour>> searchTours(String q) { return repo.searchTours(q); }
}
