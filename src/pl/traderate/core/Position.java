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
import java.util.ArrayList;

abstract class Position extends PerformanceData implements IdentifiableByName, Comparable<Position> {

	protected Holding parent;
	
	protected String name;

	protected boolean closed;

	Position(String name, boolean closed) {
		this.name = name;
		this.closed = closed;
	}

	public String getName() {
		return name;
	}

	abstract void update();

	@Override
	public int compareTo(Position o) {
		return this.name.compareTo(o.name);
	}

	Holding getParent() {
		return parent;
	}

	void setParent(Holding holding) {
		parent = holding;
	}

	boolean isClosed() {
		return closed;
	}
}
