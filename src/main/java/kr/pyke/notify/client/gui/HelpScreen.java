package kr.pyke.notify.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import kr.pyke.notify.client.state.HelpClientState;
import kr.pyke.notify.client.cache.NameClientCache;
import kr.pyke.notify.data.request.HelpRequest;
import kr.pyke.notify.network.NotifyPacket;
import kr.pyke.notify.util.constants.HELP_STATUS;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class HelpScreen extends BaseOwoScreen<FlowLayout> {
    // 컬럼 폭
    private static final int COL_NICK_PCT       = 18;
    private static final int COL_STAT_PCT       = 12;
    private static final int COL_HAND_PCT       = 18;
    private static final int COL_MSG_PCT        = 52;

    // 우측 버튼 패널 폭 비율
    private static final int RIGHT_BAR_PCT      = 18;

    // 색상 팔레트(어두운 회색 톤)
    private static final int COL_BG             = 0xFF2F2F2F;
    private static final int COL_PANEL          = 0xFF3A3A3A;
    private static final int COL_BORDER         = 0xFF6E6E6E;
    private static final int COL_TEXT           = 0xFFEAEAEA;
    private static final int COL_TEXT_DIM       = 0xFFC8C8C8;
    private static final int OUTER_THICKNESS    = 2;

    private HELP_STATUS currentFilter = null;
    private UUID selectedID = null;

    private FlowLayout listBox;
    private ButtonComponent btnPending, btnProcessing, btnResolved, btnPurge;
    private Consumer<List<HelpRequest>> listener;
    private Runnable nameListener;

    // ── 어댑터 생성 (FlowLayout 루트 사용)
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    // 테두리 두께를 래퍼로 구현(정수색 사용)
    private FlowLayout wrapWithBorder(io.wispforest.owo.ui.core.Component inner, int thickness) {
        FlowLayout outer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        outer.surface(Surface.flat(COL_BORDER));
        outer.padding(Insets.of(thickness)); // thickness 제대로 반영
        outer.child(inner);
        return outer;
    }

    // ── UI 구성
    @Override
    protected void build(FlowLayout root) {
        // 루트 컨테이너 자체는 배경만 담당, 실제 내용은 테두리 래퍼로 싸서 넣음
        root.sizing(Sizing.fill(100), Sizing.fill(100));
        root.horizontalAlignment(HorizontalAlignment.CENTER);
        root.verticalAlignment(VerticalAlignment.CENTER);
        root.surface(Surface.BLANK);
        root.gap(0);
        root.padding(Insets.of(0));

        // 실제 컨텐츠 루트
        FlowLayout content = Containers.verticalFlow(Sizing.fill(92), Sizing.fill(80));
        content.gap(8);
        content.surface(Surface.flat(COL_BG));
        content.padding(Insets.of(8));

        // ── 상단 필터 바
        FlowLayout top = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        top.gap(8);
        top.surface(Surface.flat(COL_PANEL));
        top.padding(Insets.of(6));
        top.child(filterBtn("전체", null));
        top.child(filterBtn("접수", HELP_STATUS.PENDING));
        top.child(filterBtn("진행중", HELP_STATUS.PROCESSING));
        top.child(filterBtn("완료", HELP_STATUS.RESOLVED));

        btnPurge = Components.button(net.minecraft.network.chat.Component.literal("정리"), b -> NotifyPacket.requestPurgeResolved());
        btnPurge.sizing(Sizing.fixed(48), Sizing.fixed(22));
        btnPurge.renderer(ButtonComponent.Renderer.flat(0xFF404040, 0xFF4C4C4C, 0xFF292929));
        btnPurge.active(true);
        top.child(btnPurge);

        content.child(wrapWithBorder(top, 1));

        // ── 본문: 좌(헤더+스크롤) / 우(세로 버튼)
        FlowLayout body = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));
        body.gap(8);

        // 좌측 헤더
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        header.gap(6);
        header.surface(Surface.flat(COL_PANEL));
        header.padding(Insets.of(4));
        header.child(colHeader("닉네임", COL_NICK_PCT));
        header.child(colHeader("상태",   COL_STAT_PCT));
        header.child(colHeader("처리자", COL_HAND_PCT));
        header.child(colHeader("메시지", COL_MSG_PCT));

        // 리스트 컨테이너
        listBox = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        listBox.gap(0);

        // 스크롤 컨테이너(3-인자 오버로드)
        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), listBox);
        scroll.surface(Surface.flat(COL_PANEL));

        FlowLayout left = Containers.verticalFlow(Sizing.fill(100 - RIGHT_BAR_PCT), Sizing.fill(100));
        left.child(wrapWithBorder(header, 1));

        FlowLayout scrollWrap = wrapWithBorderFillY(scroll, 1);
        left.child(scrollWrap);

        // 우측 세로 버튼 패널
        FlowLayout right = Containers.verticalFlow(Sizing.fill(RIGHT_BAR_PCT - 2), Sizing.fill(100));
        right.gap(8);
        right.surface(Surface.flat(COL_PANEL));
        right.padding(Insets.of(6));
        btnPending    = stateBtn("접수",   () -> changeStatusSelected(HELP_STATUS.PENDING));
        btnProcessing = stateBtn("처리", () -> HelpProcessing(HelpClientState.findById(selectedID)));
        btnResolved   = stateBtn("완료",   () -> changeStatusSelected(HELP_STATUS.RESOLVED));
        right.child(btnPending).child(btnProcessing).child(btnResolved);
        FlowLayout rightWrap = wrapWithBorder(right, 1);

        body.child(left);
        body.child(rightWrap);
        content.child(body);

        // 최종: content에 외곽 테두리 적용 후 루트에 붙임
        root.child(wrapWithBorder(content, OUTER_THICKNESS));

        // ── 초기 동기화 및 구독
        NotifyPacket.requestInitSync();
        this.listener = ignored -> refresh();
        HelpClientState.addListener(this.listener);

        this.nameListener = this::refresh;
        NameClientCache.addListener(this.nameListener);

        refresh();
    }

    @Override
    public void removed() {
        super.removed();
        if (null != listener) { HelpClientState.removeListener(listener); }
        if (null != nameListener) { NameClientCache.removeListener(nameListener); }
    }

    private ButtonComponent filterBtn(String name, HELP_STATUS status) {
        ButtonComponent bc = Components.button(net.minecraft.network.chat.Component.literal(name), btn -> {
            currentFilter = status;
            selectedID = null;
            refresh();
        });
        bc.sizing(Sizing.fixed(72), Sizing.fixed(22));
        bc.renderer(ButtonComponent.Renderer.flat(0xFF404040, 0xFF4C4C4C, 0xFF292929));
        return bc;
    }

    private LabelComponent colHeader(String text, int width) {
        LabelComponent lbl = Components.label(net.minecraft.network.chat.Component.literal(text));
        lbl.sizing(Sizing.fill(width), Sizing.content());
        lbl.color(Color.ofArgb(COL_TEXT));
        lbl.horizontalTextAlignment(HorizontalAlignment.LEFT);
        lbl.shadow(true);
        return lbl;
    }

    private LabelComponent colHeaderFill(String text) {
        LabelComponent lbl = Components.label(net.minecraft.network.chat.Component.literal(text));
        lbl.sizing(Sizing.fill(100), Sizing.content()); // 남은 너비 전부 사용
        lbl.color(Color.ofArgb(COL_TEXT));
        lbl.horizontalTextAlignment(HorizontalAlignment.LEFT);
        lbl.shadow(true);
        return lbl;
    }

    private LabelComponent colText(String text, int width) {
        LabelComponent lbl = Components.label(net.minecraft.network.chat.Component.literal(text));
        lbl.sizing(Sizing.fill(width), Sizing.content());
        lbl.color(Color.ofArgb(COL_TEXT_DIM));
        return lbl;
    }

    private LabelComponent colText(net.minecraft.network.chat.Component text, int width) {
        LabelComponent lbl = Components.label(null != text ? text : net.minecraft.network.chat.Component.literal("-"));
        lbl.sizing(Sizing.fill(width), Sizing.content());
        lbl.color(Color.ofArgb(COL_TEXT_DIM));
        return lbl;
    }

    private LabelComponent colTextFill(String text) {
        LabelComponent lbl = Components.label(net.minecraft.network.chat.Component.literal(text));
        lbl.sizing(Sizing.fill(100), Sizing.content());
        lbl.color(Color.ofArgb(COL_TEXT_DIM));
        lbl.horizontalTextAlignment(HorizontalAlignment.LEFT);
        return lbl;
    }

    private ButtonComponent stateBtn(String name, Runnable onClick) {
        ButtonComponent bc = Components.button(net.minecraft.network.chat.Component.literal(name), btn -> onClick.run());
        bc.sizing(Sizing.fill(100), Sizing.fixed(24));
        bc.renderer(ButtonComponent.Renderer.flat(0xFF404040, 0xFF4C4C4C, 0xFF292929));
        return bc;
    }

    private String statusKo(HelpRequest request) {
        return switch (request.status) {
            case PENDING -> "접수";
            case PROCESSING -> "진행중";
            case RESOLVED -> "완료";
        };
    }

    private boolean pass(HelpRequest request) {
        return null == currentFilter || request.status == currentFilter;
    }

    private void changeStatusSelected(HELP_STATUS status) {
        if (null == selectedID) { return; }
        NotifyPacket.requestSetStatus(selectedID, status);
    }

    private void refresh() {
        boolean hasSel = null != selectedID;
        if (null != btnPending)    { btnPending.active(hasSel); }
        if (null != btnProcessing) { btnProcessing.active(hasSel); }
        if (null != btnResolved)   { btnResolved.active(hasSel); }
        if (null != btnPurge)      { btnPurge.active(true); }

        listBox.clearChildren();

        for (HelpRequest request : HelpClientState.snapshot()) {
            if (!pass(request)) { continue; }

            boolean selected = request.requestUUID.equals(selectedID);

            FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
            row.gap(6);
            row.surface(Surface.flat(selected ? 0xFF444444 : COL_PANEL));
            row.padding(Insets.of(2));

            row.mouseDown().subscribe((mx, my, btn) -> {
                selectedID = selected ? null : request.requestUUID;
                refresh();
                return true;
            });

            Component senderName = NameClientCache.getOrDefault(request.senderUUID, Component.literal("Unknown"));

            Component handlerName;
            if (request.status != HELP_STATUS.PENDING && request.handlerUUID != null) { handlerName = NameClientCache.getOrDefault(request.handlerUUID, Component.literal("Unknown")); }
            else { handlerName = Component.literal("—"); }

            String message = (null == request.message || request.message.isBlank()) ? "" : request.message;

            row.child(colText(senderName,  COL_NICK_PCT));
            row.child(colText(statusKo(request), COL_STAT_PCT));
            row.child(colText(handlerName, COL_HAND_PCT));
            row.child(colText(message, COL_MSG_PCT));

            listBox.child(wrapWithBorder(row, 1));
        }

        requestMissingNamesForVisibleRows();
    }

    private void requestMissingNamesForVisibleRows() {
        var need = new ArrayList<UUID>();
        for (HelpRequest request : HelpClientState.snapshot()) {
            if (!pass(request)) { continue; }
            need.add(request.senderUUID);
            if (null != request.handlerUUID) need.add(request.handlerUUID);
        }
        var miss = NameClientCache.missing(need);
        NotifyPacket.requestNames(miss);
    }

    private FlowLayout wrapWithBorderFillY(io.wispforest.owo.ui.core.Component inner, int thickness) {
        inner.sizing(Sizing.fill(100), Sizing.fill(100));
        FlowLayout outer = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        outer.surface(Surface.flat(COL_BORDER));
        outer.padding(Insets.of(thickness));
        outer.child(inner);
        return outer;
    }

    private void HelpProcessing(HelpRequest request) {
        if (null == selectedID || null == request) { return; }

        changeStatusSelected(HELP_STATUS.PROCESSING);
        NotifyPacket.requestTeleportTo(request.senderUUID);
    }
}
