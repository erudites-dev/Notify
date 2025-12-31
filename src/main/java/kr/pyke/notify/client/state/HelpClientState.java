package kr.pyke.notify.client.state;

import kr.pyke.notify.data.request.HelpRequest;
import kr.pyke.notify.util.constants.HELP_STATUS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class HelpClientState {
    private static final List<HelpRequest> LIST = new CopyOnWriteArrayList<>();
    private static final List<Consumer<List<HelpRequest>>> LISTENERS = new ArrayList<>();

    public static void addListener(Consumer<List<HelpRequest>> listener) { LISTENERS.add(listener); }
    public static void removeListener(Consumer<List<HelpRequest>> listener) { LISTENERS.remove(listener); }

    private static void notifyListeners() {
        var snap = List.copyOf(LIST);
        for (var listener : LISTENERS) { listener.accept(snap); }
    }

    public static void onFullSync(List<HelpRequest> fromServer) {
        LIST.clear();
        LIST.addAll(fromServer);
        notifyListeners();
    }

    public static void onAppended(HelpRequest request) {
        LIST.add(request);
        notifyListeners();
    }

    public static void onUpdated(HelpRequest request) {
        for(int i = 0; i < LIST.size(); i++) {
            if (LIST.get(i).requestUUID.equals(request.requestUUID)) {
                LIST.set(i, request);
                break;
            }
        }
        notifyListeners();
    }

    public static List<HelpRequest> snapshot() {
        return List.copyOf(LIST);
    }

    public static long RequestCount() {
        return LIST.stream().filter(request -> request.status == HELP_STATUS.PENDING).count();
    }

    public static HelpRequest findById(UUID selectedID) {
        for (HelpRequest request : snapshot()) {
            if (selectedID.equals(request.requestUUID)) { return request; }
        }
        return null;
    }
}