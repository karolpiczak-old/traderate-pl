/*
 * Copyright (C) 2012 Karol Piczak <karol@dvl.pl>
 *
 * This file is part of the TradeRate package.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package pl.traderate.desktop;

import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.painter.border.*;
import org.pushingpixels.substance.api.painter.decoration.MatteDecorationPainter;
import org.pushingpixels.substance.api.painter.fill.FractionBasedFillPainter;
import org.pushingpixels.substance.api.painter.highlight.ClassicHighlightPainter;
import org.pushingpixels.substance.api.painter.overlay.BottomLineOverlayPainter;
import org.pushingpixels.substance.api.painter.overlay.BottomShadowOverlayPainter;
import org.pushingpixels.substance.api.painter.overlay.TopBezelOverlayPainter;
import org.pushingpixels.substance.api.painter.overlay.TopLineOverlayPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;

import java.awt.*;

/**
 * Modified <code>Magellan</code> skin. This class is part of officially supported API.
 * 
 * @author Kirill Grouchnikov
 * @since version 5.3
 */
public class TradeRateSkin extends SubstanceSkin {
	/**
	 * Display name for <code>this</code> skin.
	 */
	public static final String NAME = "TradeRate (Magellan)";

	/**
	 * Overlay painter to paint a dark line along the bottom edge of the
	 * toolbars.
	 */
	private BottomLineOverlayPainter toolbarBottomLineOverlayPainter;

	/**
	 * Overlay painter to paint a light line along the top edge of the toolbars.
	 */
	private TopLineOverlayPainter toolbarTopLineOverlayPainter;

	/**
	 * Overlay painter to paint a bezel line along the top edge of the footer.
	 */
	private TopBezelOverlayPainter footerTopBezelOverlayPainter;

	@Override
	public String getDisplayName() {
		return NAME;
	}

	/**
	 * Creates a new instance of Magellan skin.
	 */
	public TradeRateSkin() {
		ColorSchemes colorSchemes = SubstanceSkin
				.getColorSchemes("org/pushingpixels/substance/api/skin/magellan.colorschemes");

		SubstanceColorScheme blueControlsActive = colorSchemes
				.get("Magellan Blue Controls Active").shift(new Color(21, 34, 42), 0.7, new Color(232, 232, 232), 1.0);
		SubstanceColorScheme blueControlsEnabled = colorSchemes
				.get("Magellan Blue Controls Enabled").shift(new Color(21, 34, 42), 0.7, new Color(232, 232, 232), 0.8);

		SubstanceColorSchemeBundle defaultColorSchemeBundle = new SubstanceColorSchemeBundle(
				blueControlsActive, blueControlsEnabled, blueControlsEnabled);
		defaultColorSchemeBundle.registerColorScheme(blueControlsEnabled, 0.5f,
				ComponentState.DISABLED_UNSELECTED);
		defaultColorSchemeBundle.registerColorScheme(blueControlsActive, 0.5f,
				ComponentState.DISABLED_SELECTED);

		// color schemes for the active states
		SubstanceColorScheme blueControlsActiveBorder = colorSchemes
				.get("Magellan Blue Controls Active Border").shift(new Color(21, 34, 42), 0.7, new Color(232, 232, 232), 0.8);
		SubstanceColorScheme blueControlsEnabledBorder = colorSchemes
				.get("Magellan Blue Controls Enabled Border").shift(new Color(21, 34, 42), 0.7, new Color(232, 232, 232), 0.8);
		defaultColorSchemeBundle.registerColorScheme(blueControlsActiveBorder,
				ColorSchemeAssociationKind.BORDER, ComponentState
						.getActiveStates());
		defaultColorSchemeBundle.registerColorScheme(blueControlsActiveBorder,
				ColorSchemeAssociationKind.BORDER,
				ComponentState.DISABLED_SELECTED);
		defaultColorSchemeBundle.registerColorScheme(blueControlsEnabledBorder,
				ColorSchemeAssociationKind.BORDER, ComponentState.ENABLED,
				ComponentState.DISABLED_UNSELECTED);

		// color schemes for the pressed states
		SubstanceColorScheme blueControlsPressed = colorSchemes
				.get("Magellan Blue Controls Pressed").shift(new Color(21, 34, 42), 0.7, new Color(21, 34, 42), 0.0);
		SubstanceColorScheme blueControlsPressedBorder = colorSchemes
				.get("Magellan Blue Controls Pressed Border").shift(new Color(21, 34, 42), 0.7, new Color(21, 34, 42), 0.0);
		defaultColorSchemeBundle.registerColorScheme(blueControlsPressed,
				ColorSchemeAssociationKind.FILL,
				ComponentState.PRESSED_SELECTED,
				ComponentState.PRESSED_UNSELECTED);
		defaultColorSchemeBundle.registerColorScheme(blueControlsPressedBorder,
				ColorSchemeAssociationKind.BORDER,
				ComponentState.PRESSED_SELECTED,
				ComponentState.PRESSED_UNSELECTED);

		// color schemes for the rollover / armed states
		SubstanceColorScheme greenControls = colorSchemes
				.get("Magellan Green Controls").shift(new Color(239, 102, 3), 0.85, new Color(239, 115, 3), 0.0);
		SubstanceColorScheme greenControlsMark = colorSchemes
				.get("Magellan Green Controls Mark").shift(new Color(239, 102, 3), 0.85, new Color(239, 115, 3), 0.0);
		SubstanceColorScheme greenControlsBorder = colorSchemes
				.get("Magellan Green Controls Border").shift(new Color(239, 102, 3), 0.85, new Color(239, 115, 3), 0.0);
		defaultColorSchemeBundle.registerColorScheme(greenControls,
				ColorSchemeAssociationKind.FILL,
				ComponentState.ROLLOVER_SELECTED,
				ComponentState.ROLLOVER_UNSELECTED, ComponentState.ARMED,
				ComponentState.ROLLOVER_ARMED);
		defaultColorSchemeBundle.registerColorScheme(greenControlsMark,
				ColorSchemeAssociationKind.MARK,
				ComponentState.ROLLOVER_SELECTED,
				ComponentState.ROLLOVER_UNSELECTED, ComponentState.ARMED,
				ComponentState.ROLLOVER_ARMED);
		defaultColorSchemeBundle.registerColorScheme(greenControlsBorder,
				ColorSchemeAssociationKind.BORDER,
				ComponentState.ROLLOVER_SELECTED,
				ComponentState.ROLLOVER_UNSELECTED, ComponentState.ARMED,
				ComponentState.ROLLOVER_ARMED);

		// color scheme for the uneditable text components
		ComponentState uneditable = new ComponentState("uneditable",
				new ComponentStateFacet[] { ComponentStateFacet.ENABLE },
				new ComponentStateFacet[] { ComponentStateFacet.EDITABLE });
		SubstanceColorScheme uneditableControls = colorSchemes
				.get("Magellan Uneditable Controls").shift(new Color(21, 34, 42), 0.7, new Color(21, 34, 42), 0.0);
		defaultColorSchemeBundle.registerColorScheme(uneditableControls,
				ColorSchemeAssociationKind.FILL, uneditable);

		// color scheme for the selected state - preventing fallback to the
		// rollover selected state
		defaultColorSchemeBundle.registerColorScheme(blueControlsActive,
				ColorSchemeAssociationKind.FILL, ComponentState.SELECTED);

		// highlight alphas
		defaultColorSchemeBundle.registerHighlightColorScheme(greenControls,
				0.7f, ComponentState.ROLLOVER_UNSELECTED);
		defaultColorSchemeBundle.registerHighlightColorScheme(greenControls,
				0.8f, ComponentState.SELECTED);
		defaultColorSchemeBundle.registerHighlightColorScheme(greenControls,
				0.95f, ComponentState.ROLLOVER_SELECTED);
		defaultColorSchemeBundle.registerHighlightColorScheme(greenControls,
				1.0f, ComponentState.ARMED, ComponentState.ROLLOVER_ARMED);

		SubstanceColorScheme lightBlueBackground = colorSchemes
				.get("Magellan Light Blue Background").shift(new Color(21, 34, 42), 0.8, new Color(21, 34, 42), 0.0);;

		this.registerDecorationAreaSchemeBundle(defaultColorSchemeBundle,
				lightBlueBackground, DecorationAreaType.NONE);

		SubstanceColorScheme mediumBlueBackground = colorSchemes
				.get("Magellan Medium Blue Background").shift(new Color(21, 34, 42), 0.8, new Color(21, 34, 42), 0.0);
		SubstanceColorScheme darkBlueBackground = colorSchemes
				.get("Magellan Dark Blue Background").shift(new Color(21, 34, 42), 0.8, new Color(21, 34, 42), 0.0);
		this.registerAsDecorationArea(mediumBlueBackground,
				DecorationAreaType.GENERAL, DecorationAreaType.TOOLBAR);
		this.registerAsDecorationArea(darkBlueBackground,
				DecorationAreaType.PRIMARY_TITLE_PANE,
				DecorationAreaType.SECONDARY_TITLE_PANE,
				DecorationAreaType.HEADER);

		SubstanceColorScheme lightBlueControlsActive = colorSchemes
				.get("Magellan Light Blue Controls Active").shift(new Color(21, 34, 42), 0.7, new Color(21, 34, 42), 0.0);
		SubstanceColorScheme lightBlueControlsEnabled = colorSchemes
				.get("Magellan Light Blue Controls Enabled").shift(new Color(21, 34, 42), 0.7, new Color(21, 34, 42), 0.0);
		SubstanceColorScheme lightBlueBordersEnabled = colorSchemes
				.get("Magellan Light Blue Borders Enabled").shift(new Color(21, 34, 42), 0.7, new Color(21, 34, 42), 0.0);
		SubstanceColorSchemeBundle footerColorSchemeBundle = new SubstanceColorSchemeBundle(
				lightBlueControlsActive, lightBlueControlsEnabled,
				lightBlueControlsEnabled);
		footerColorSchemeBundle.registerColorScheme(lightBlueControlsEnabled,
				0.5f, ComponentState.DISABLED_UNSELECTED);
		footerColorSchemeBundle.registerColorScheme(lightBlueControlsActive,
				0.5f, ComponentState.DISABLED_SELECTED);
		footerColorSchemeBundle.registerColorScheme(lightBlueBordersEnabled,
				ColorSchemeAssociationKind.BORDER, ComponentState.ENABLED);

		SubstanceColorScheme lightBlueSeparator = colorSchemes
				.get("Magellan Light Blue Separator").shift(new Color(21, 34, 42), 0.7, new Color(21, 34, 42), 0.0);
		footerColorSchemeBundle.registerColorScheme(lightBlueSeparator,
				ColorSchemeAssociationKind.SEPARATOR);

		SubstanceColorScheme ultraLightBlueBackground = colorSchemes
				.get("Magellan Ultralight Blue Background").shift(new Color(21, 34, 42), 0.7, new Color(21, 34, 42), 0.0);
		this.registerDecorationAreaSchemeBundle(footerColorSchemeBundle,
				ultraLightBlueBackground, DecorationAreaType.FOOTER);

		// Add overlay painter to paint drop shadows along the bottom
		// edges of toolbars
		this.addOverlayPainter(BottomShadowOverlayPainter.getInstance(),
				DecorationAreaType.TOOLBAR);

		// add an overlay painter to paint a dark line along the bottom
		// edge of toolbars
		this.toolbarBottomLineOverlayPainter = new BottomLineOverlayPainter(
				new ColorSchemeSingleColorQuery() {
					@Override
					public Color query(SubstanceColorScheme scheme) {
						return scheme.getUltraDarkColor();
					}
				});
		this.addOverlayPainter(this.toolbarBottomLineOverlayPainter,
				DecorationAreaType.TOOLBAR);

		// add an overlay painter to paint a light line along the top
		// edge of toolbars
		this.toolbarTopLineOverlayPainter = new TopLineOverlayPainter(
				new ColorSchemeSingleColorQuery() {
					@Override
					public Color query(SubstanceColorScheme scheme) {
						Color fg = scheme.getForegroundColor();
						return new Color(fg.getRed(), fg.getGreen(), fg
								.getBlue(), 40);
					}
				});
		this.addOverlayPainter(this.toolbarTopLineOverlayPainter,
				DecorationAreaType.TOOLBAR);

		// add an overlay painter to paint a bezel line along the top
		// edge of footer
		this.footerTopBezelOverlayPainter = new TopBezelOverlayPainter(
				ColorSchemeSingleColorQuery.FOREGROUND,
				ColorSchemeSingleColorQuery.ULTRALIGHT);
		this.addOverlayPainter(this.footerTopBezelOverlayPainter,
				DecorationAreaType.FOOTER);

		setSelectedTabFadeStart(0.2);
		setSelectedTabFadeEnd(0.9);

		SubstanceBorderPainter outerBorderPainter = new FractionBasedBorderPainter(
				"Magellan Outer", new float[] { 0.0f, 0.5f, 1.0f },
				new ColorSchemeSingleColorQuery[] {
						ColorSchemeSingleColorQuery.ULTRADARK,
						ColorSchemeSingleColorQuery.DARK,
						ColorSchemeSingleColorQuery.DARK });
		SubstanceBorderPainter innerBorderPainter = new DelegateBorderPainter(
				"Magellan Inner", new ClassicBorderPainter(), 0xA0FFFFFF,
				0x60FFFFFF, 0x40FFFFFF, new ColorSchemeTransform() {
					@Override
					public SubstanceColorScheme transform(
							SubstanceColorScheme scheme) {
						return scheme.tint(0.5);
					}
				});
		this.borderPainter = new CompositeBorderPainter("Magellan",
				outerBorderPainter, innerBorderPainter);
		this.fillPainter = new FractionBasedFillPainter("Magellan",
				new float[] { 0.0f, 0.5f, 1.0f },
				new ColorSchemeSingleColorQuery[] {
						ColorSchemeSingleColorQuery.EXTRALIGHT,
						ColorSchemeSingleColorQuery.LIGHT,
						ColorSchemeSingleColorQuery.MID });
		this.highlightPainter = new ClassicHighlightPainter();
		this.decorationPainter = new MatteDecorationPainter();
		this.buttonShaper = new ClassicButtonShaper();
	}
}
