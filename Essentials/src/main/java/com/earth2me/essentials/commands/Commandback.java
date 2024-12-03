package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class Commandback extends EssentialsCommand {
    public Commandback() {
        super("back");
    }

    @Override
    protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        final CommandSource sender = user.getSource();
        if (args.length > 0 && user.isAuthorized("essentials.back.others")) {
            parseOthers(server, sender, args, commandLabel);
            return;
        }

        if (args.length == 1 && "confirm".equals(args[0])){
            teleportBack(sender, user, commandLabel, true);
        }else{
            teleportBack(sender, user, commandLabel, false);
        }
    }

    @Override
    protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length == 0) {
            throw new NotEnoughArgumentsException();
        }

        parseOthers(server, sender, args, commandLabel);
    }

    private void parseOthers(final Server server, final CommandSource sender, final String[] args, final String commandLabel) throws Exception {
        final User player = getPlayer(server, args, 0, true, false);
        sender.sendTl("backOther", player.getName());
        teleportBack(sender, player, commandLabel, true);
    }

    private void teleportBack(final CommandSource sender, final User user, final String commandLabel, boolean confirmed) throws Exception {
        if (user.getLastLocation() == null) {
            throw new TranslatableException("noLocationFound");
        }

        final String lastWorldName = user.getLastLocation().getWorld().getName();

        User requester = null;
        if (sender.isPlayer()) {
            requester = ess.getUser(sender.getPlayer());

            if (user.getWorld() != user.getLastLocation().getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + lastWorldName)) {
                throw new TranslatableException("noPerm", "essentials.worlds." + lastWorldName);
            }

            if (!requester.isAuthorized("essentials.back.into." + lastWorldName)) {
                throw new TranslatableException("noPerm", "essentials.back.into." + lastWorldName);
            }
        }
        final Trade charge = new Trade(this.getName(), this.ess);
        final BigDecimal cost = charge.getCommandCost(user);
        if (!cost.equals(BigDecimal.ZERO) && !confirmed && ess.getSettings().getConfirmCommandCost()){
            throw new TranslatableException("confirmCommandCost", this.getName(), NumberUtil.shortCurrency(cost, ess));
        }
        if (requester == null) {
            user.getAsyncTeleport().back(null, null, getNewExceptionFuture(sender, commandLabel));
        } else if (!requester.equals(user)) {
            charge.isAffordableFor(requester);
            user.getAsyncTeleport().back(requester, charge, getNewExceptionFuture(sender, commandLabel));
        } else {
            charge.isAffordableFor(user);
            user.getAsyncTeleport().back(charge, getNewExceptionFuture(sender, commandLabel));
        }
        throw new NoChargeException();
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
        if (user.isAuthorized("essentials.back.others") && args.length == 1) {
            return getPlayers(server, user);
        } else {
            return Collections.emptyList();
        }
    }
}
