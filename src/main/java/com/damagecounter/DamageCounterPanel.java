/*
 * Copyright (c) 2018 Abex
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2021 0anth <https://github.com/0anth>
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

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import net.runelite.api.Client;
import net.runelite.client.events.SessionClose;
import net.runelite.client.events.SessionOpen;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.account.SessionManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.info.JRichTextPane;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

@Singleton
public class DamageCounterPanel extends PluginPanel
{
	private static final ImageIcon ARROW_RIGHT_ICON;
	private static final ImageIcon GITHUB_ICON;

	private JPanel actionsContainer;
	private JPanel knownIssuesContainer;
	private JPanel changelogContainer;

	@Inject
	@Nullable
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private ConfigManager configManager;

	private static final String changelogText = "<html><body style = 'color:#a5a5a5'>" +
		"<span style = 'color:white'>Changelog</span><br><br>" +
		"<span style = 'color:white'>v1.6 -- 17-Jan-2021</span><br>" +
		"- Added side panel (to be expanded upon soon)<br><br>" +
		"<span style = 'color:white'>v1.5 -- 09-Dec-2020</span><br>" +
		"- Counter now automatically resets when engaging combat with a different boss<br>" +
		"- Counter will no longer automatically reset when teleporting/running away from boss<br>" +
		"- Counter will still automatically reset when boss dies<br>" +
		"- Extra config option added to allow custom NPCs to be tracked by name<br><br>" +
		"<span style = 'color:white'>v1.4.4 -- 08-Dec-2020</span><br>" +
		"- Bugfix for Giant Mole<br><br>" +
		"<span style = 'color:white'>v1.4.3 -- 03-Dec-2020</span><br>" +
		"- Bugfix for conflicting class names<br>" +
		"- Counter will now reset if you run away or teleport from combat<br><br>" +
		"<span style = 'color:white'>v1.4.2 -- 25-Nov-2020</span><br>" +
		"- Bugfix for Barrows again<br>" +
		"- Shortened chat log message<br>" +
		"- Chat log message will now only mention percentage, if it is less than 100%<br><br>" +
		"<span style = 'color:white'>v1.4.1 -- 18-Nov-2020</span><br>" +
		"- Bugfix for counter not resetting on boss death<br><br>" +
		"<span style = 'color:white'>v1.4 -- 11-Nov-2020</span><br>" +
		"- Included config option to always hide overlay<br><br>" +
		"<span style = 'color:white'>v1.3 -- 11-Nov-2020</span><br>" +
		"- Included kill time in overlay, rather than mouseover<br>" +
		"- Fixed bug with Barrows brothers<br><br>" +
		"<span style = 'color:white'>v1.2 -- 10-Nov-2020</span><br>" +
		"- Added kill time to chat log message<br><br>" +
		"<span style = 'color:white'>v1.1 -- 04-Nov-2020</span><br>" +
		"- Added config option to automatically hide overlay when boss dies<br><br>" +
		"<span style = 'color:white'>v1.0 -- 27-Oct-2020</span><br>" +
		"- Initial commit" +
		"</body></html>";

	private static final String knownIssuesText = "<html><body style = 'color:#a5a5a5'>" +
		"<span style = 'color:white'>Known issues</span><br><br>" +
		"- Grotesque Guardians logging incorrectly" +
		"</body></html>";

	static
	{
		ARROW_RIGHT_ICON = new ImageIcon(ImageUtil.getResourceStreamFromClass(DamageCounterPlugin.class, "arrow_right.png"));
		GITHUB_ICON = new ImageIcon(ImageUtil.getResourceStreamFromClass(DamageCounterPlugin.class, "github_icon.png"));
	}

	void init()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// Begin changelog

		changelogContainer = new JPanel();
		changelogContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
		changelogContainer.setLayout(new GridLayout(0, 1, 0, 10));

		changelogContainer.add(buildTextPanel(changelogText));

		// End changelog
		// Start known issues

		knownIssuesContainer = new JPanel();
		knownIssuesContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
		knownIssuesContainer.setLayout(new GridLayout(0, 1, 0, 10));

		knownIssuesContainer.add(buildTextPanel(knownIssuesText));

		// End known issues
		// Begin actions container

		actionsContainer = new JPanel();
		actionsContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
		actionsContainer.setLayout(new GridLayout(0, 1, 0, 10));

		actionsContainer.add(buildLinkPanel(GITHUB_ICON, "Report an issue or", "make a suggestion", "https://github.com/0anth/damage-counter/issues/new"));

		// End action container

		add(actionsContainer, BorderLayout.NORTH);
		add(knownIssuesContainer, BorderLayout.CENTER);
		add(changelogContainer, BorderLayout.SOUTH);

		eventBus.register(this);
	}

	/**
	 * Builds a link panel with a given icon, text and url to redirect to.
	 */
	private static JPanel buildLinkPanel(ImageIcon icon, String topText, String bottomText, String url)
	{
		return buildLinkPanel(icon, topText, bottomText, () -> LinkBrowser.browse(url));
	}

	/**
	 * Builds a link panel with a given icon, text and callable to call.
	 */
	private static JPanel buildLinkPanel(ImageIcon icon, String topText, String bottomText, Runnable callback)
	{
		JPanel container = new JPanel();
		container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		container.setLayout(new BorderLayout());
		container.setBorder(new EmptyBorder(10, 10, 10, 10));

		final Color hoverColor = ColorScheme.DARKER_GRAY_HOVER_COLOR;
		final Color pressedColor = ColorScheme.DARKER_GRAY_COLOR.brighter();

		JLabel iconLabel = new JLabel(icon);
		container.add(iconLabel, BorderLayout.WEST);

		JPanel textContainer = new JPanel();
		textContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		textContainer.setLayout(new GridLayout(2, 1));
		textContainer.setBorder(new EmptyBorder(5, 10, 5, 10));

		container.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				container.setBackground(pressedColor);
				textContainer.setBackground(pressedColor);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				callback.run();
				container.setBackground(hoverColor);
				textContainer.setBackground(hoverColor);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				container.setBackground(hoverColor);
				textContainer.setBackground(hoverColor);
				container.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				textContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				container.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});

		JLabel topLine = new JLabel(topText);
		topLine.setForeground(Color.WHITE);
		topLine.setFont(FontManager.getRunescapeSmallFont());

		JLabel bottomLine = new JLabel(bottomText);
		bottomLine.setForeground(Color.WHITE);
		bottomLine.setFont(FontManager.getRunescapeSmallFont());

		textContainer.add(topLine);
		textContainer.add(bottomLine);

		container.add(textContainer, BorderLayout.CENTER);

		JLabel arrowLabel = new JLabel(ARROW_RIGHT_ICON);
		container.add(arrowLabel, BorderLayout.EAST);

		return container;
	}

	private static JPanel buildTextPanel(String text)
	{
		JPanel container = new JPanel();
		container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		container.setLayout(new GridLayout(0, 1));
		container.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel textLabel = new JLabel(text);
		textLabel.setForeground(Color.WHITE);
		textLabel.setFont(FontManager.getRunescapeFont());

		container.add(textLabel);

		return container;
	}
}

