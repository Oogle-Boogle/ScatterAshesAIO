package com.Bing_Bong.scripts.InfernalScatter;

import com.runemate.game.api.client.ClientUI;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.input.direct.DirectInput;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import com.runemate.game.api.hybrid.location.navigation.Landmark;
import com.runemate.game.api.hybrid.region.Banks;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.util.Items;
import com.runemate.game.api.hybrid.util.calculations.Distance;
import com.runemate.game.api.hybrid.web.WebPath;
import com.runemate.game.api.hybrid.web.WebPathRequest;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingBot;
import com.runemate.game.api.script.framework.listeners.SettingsListener;
import com.runemate.game.api.script.framework.listeners.events.SettingChangedEvent;
import com.runemate.ui.setting.annotation.open.SettingsProvider;
import lombok.Getter;
import lombok.extern.log4j.*;

@Log4j2(topic = "InfernalScatter")
public class InfernalScatter extends LoopingBot implements SettingsListener {

    @Getter
    @SettingsProvider(updatable = true)
    private ScatterConfig config;

    State state;
    boolean started = false;

    @Override
    public void onStart(String... args) {
        getEventDispatcher().addListener(this);
        ClientUI.showAlert(ClientUI.AlertLevel.INFO, "You can join BB Discord to find out more information - <a href=\"https://discord.gg/fX5byan2mJ\">Click here</a> ");
    }

    @Override
    public void onLoop() {

        if (config == null) {
            return;
        }

        if (!started) {
            return;
        }

        final var player = Players.getLocal();
        if (player == null) {
            return;
        }

        if (state == State.BANK) {
            bank();
            return;
        }

        if (Bank.isOpen()) {
            Bank.close(true);
        }

        if (state == State.SCATTER) {
            scatter();
            return;
        }
        if (state == null) {
            log.info("State is null.");
            if (Inventory.contains(config.ashes().getGameName())) {
                state = State.SCATTER;
            } else {
                state = State.BANK;
            }
        }
    }

    private void bank() {
        final var player = Players.getLocal();
        if (player == null) {
            log.info("Bank state - player is null");
            return;
        }

        final var inventory = Inventory.getItems();

        if (Bank.isOpen()) {

            if (!Bank.contains(config.ashes().getGameName()) && !Inventory.contains(config.ashes().getGameName())) {
                String msg = "There were no " + config.ashes().getGameName() + " found in the bank.";
                log.info(msg);
                ClientUI.showAlert(ClientUI.AlertLevel.INFO, msg);
                stop(msg);
                return;
            }

            if (!Bank.contains(config.ashes().getGameName())) {
                String msg = "There were no " + config.ashes().getGameName() + " found in the bank.";
                log.info(msg);
                ClientUI.showAlert(ClientUI.AlertLevel.INFO, msg);
                stop(msg);
                return;
            }

            if (Items.containsAnyExcept(inventory, config.ashes().getGameName())) {
                Bank.depositAllExcept(config.ashes().getGameName());
                return;
            }

            if (Bank.contains(config.ashes().getGameName())) {
                Bank.withdraw(config.ashes().getGameName(), Inventory.getEmptySlots());
                Execution.delayUntil(() -> Inventory.contains(config.ashes().getGameName()), 2000);
            }

            log.info("Calling Scatter state.");
            state = State.SCATTER;
            return;
        }

        final var banks = Banks.getLoaded().first();
        if (banks == null || Distance.between(player, banks) > 30) {
            log.info("Going closer to a bank.");
            WebPath path = WebPathRequest.builder().setLandmark(Landmark.BANK).setUsingTeleports(true).build();
            if (path != null) {
                path.step();
            } else if (Distance.between(banks, player) < 15) {
                log.info("Camera turning to " + banks);
                Camera.turnTo(banks);
                return;
            }
        }

        log.info("Opening bank {}", banks);
        Bank.open();
    }

    private void scatter() {
        final var player = Players.getLocal();
        if (player == null) {
            return;
        }

        final var ashes = getAshes();

        if (ashes != null) {
            if (!Inventory.contains(config.ashes().getGameName())) {
                state = State.BANK;
            }
            if (ashes.interact("Scatter")) {
                Execution.delayUntil(() -> player.getAnimationId() == -1 && ashes.isValid(), 1800);
                return;
            }
            return;
        }
        state = State.BANK;
    }

    private SpriteItem getAshes() {
        final var item = Inventory.getItems(config.ashes().getGameName()).first();
        return item != null ? item : Inventory.getSelectedItem();
    }

    @Override
    public void onSettingChanged(SettingChangedEvent settingChangedEvent) {

    }

    @Override
    public void onSettingsConfirmed() {
        started = true;
    }

    private enum State {
        SCATTER, BANK
    }

}
