/*
 * Copyright (c) 2020 Adam <Adam@sigterm.info>
 * Copyright (c) 2020 0anth <https://github.com/0anth/damage-counter/>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.damagecounter;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.ws.PartyService;

class DamageOverlay extends OverlayPanel
{
	private static final DecimalFormat DPS_FORMAT = new DecimalFormat("#0.0");
	private static final int PANEL_WIDTH_OFFSET = 0; // switched to 0 from 10 to match the other overlays

	private final DamageCounterPlugin damageCounterPlugin;
	private final DamageCounterConfig damageCounterConfig;
	private final PartyService partyService;
	private final Client client;

	@Inject
	DamageOverlay(DamageCounterPlugin damageCounterPlugin, DamageCounterConfig damageCounterConfig, PartyService partyService, Client client)
	{
		super(damageCounterPlugin);
		this.damageCounterPlugin = damageCounterPlugin;
		this.damageCounterConfig = damageCounterConfig;
		this.partyService = partyService;
		this.client = client;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (damageCounterConfig.overlayHide())
		{
			return null;
		}

		Map<String, DamageMember> dpsMembers = damageCounterPlugin.getMembers();
		if (dpsMembers.isEmpty() || (damageCounterConfig.overlayAutoHide() && DamageMember.overlayHide))
		{
			return null;
		}

		boolean inParty = !partyService.getMembers().isEmpty();
		boolean showDamage = damageCounterConfig.showDamage();
		DamageMember total = damageCounterPlugin.getTotal();

		final String title = "Damage Counter";
		panelComponent.getChildren().add(
			TitleComponent.builder()
				.text(title)
				.build());

		int maxWidth = ComponentConstants.STANDARD_WIDTH;
		FontMetrics fontMetrics = graphics.getFontMetrics();

		for (DamageMember damageMember : dpsMembers.values())
		{
			String left = damageMember.getName();
			String right = showDamage ? QuantityFormatter.formatNumber(damageMember.getDamage()) : DPS_FORMAT.format(damageMember.getDps());
			maxWidth = Math.max(maxWidth, fontMetrics.stringWidth(left) + fontMetrics.stringWidth(right));
			panelComponent.getChildren().add(
				LineComponent.builder()
					.left(left)
					.right(right)
					.build());
		}

		panelComponent.setPreferredSize(new Dimension(maxWidth + PANEL_WIDTH_OFFSET, 0));

		if (!inParty)
		{
			Player player = client.getLocalPlayer();
			if (player.getName() != null)
			{
				DamageMember self = dpsMembers.get(player.getName());
				double damageDone = self.getDamage();
				double damageTotal = total.getDamage();
				double damagePercent = damageDone / damageTotal;
				DecimalFormat df = new DecimalFormat("##%");

				if (self != null && total.getDamage() > self.getDamage())
				{
					panelComponent.getChildren().add(
						LineComponent.builder()
							.left(total.getName())
							.right(showDamage ? Integer.toString(total.getDamage()) : DPS_FORMAT.format(total.getDps()))
							.build());
				}
			}
		}

		Duration elapsed = total.elapsed();
		long s = elapsed.getSeconds();
		String format;
		if (s >= 3600)
		{
			format = String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
		}
		else
		{
			format = String.format("%d:%02d", s / 60, (s % 60));
		}

		panelComponent.getChildren().add(
			LineComponent.builder()
				.left("Elapsed time:")
				.right(format)
				.build());

		return super.render(graphics);
	}
}
