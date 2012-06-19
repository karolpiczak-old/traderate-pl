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

/**
 * A cash allocations DTO.
 */
public abstract class CashAllocationsDTO {

	/**
	 * List of individual cash allocations.
	 */
	public ArrayList<Allocation> allocations;

	protected CashAllocationsDTO() {
		allocations = new ArrayList<>();
	}

	/**
	 * An individual cash allocation.
	 *
	 * Holds information about allocated amount and portfolio name.
	 */
	public class Allocation {

		/**
		 * Portfolio name.
		 */
		public String name;

		/**
		 * Allocated amount.
		 */
		public BigDecimal amount;

		public Allocation(BigDecimal amount, String name) {
			this.amount = amount.setScale(2);
			this.name = name;
		}
	}
}
