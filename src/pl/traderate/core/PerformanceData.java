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

package pl.traderate.core;

import java.math.BigDecimal;

/**
 * Common performance related data.
 *
 * Should be encapsulated inside higher level objects.
 */
class PerformanceData {

	protected BigDecimal quantity;

	protected BigDecimal openPrice;

	protected BigDecimal commission;

	protected BigDecimal openValue;

	protected BigDecimal closePrice;

	protected BigDecimal closeValue;

	protected BigDecimal lastMarketPrice;

	protected BigDecimal marketValue;

	protected BigDecimal paperGain;

	protected BigDecimal paperGainPercentage;

	protected BigDecimal realizedGain;

	protected BigDecimal realizedGainPercentage;

	BigDecimal getQuantity() {
		return quantity;
	}

	BigDecimal getOpenPrice() {
		return openPrice;
	}

	BigDecimal getCommission() {
		return commission;
	}

	BigDecimal getOpenValue() {
		return openValue;
	}

	BigDecimal getClosePrice() {
		return closePrice;
	}

	BigDecimal getCloseValue() {
		return closeValue;
	}

	BigDecimal getLastMarketPrice() {
		return lastMarketPrice;
	}

	BigDecimal getMarketValue() {
		return marketValue;
	}

	BigDecimal getPaperGain() {
		return paperGain;
	}

	BigDecimal getPaperGainPercentage() {
		return paperGainPercentage;
	}

	BigDecimal getRealizedGain() {
		return realizedGain;
	}

	BigDecimal getRealizedGainPercentage() {
		return realizedGainPercentage;
	}
}
