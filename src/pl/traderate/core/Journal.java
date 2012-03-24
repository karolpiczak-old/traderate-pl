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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 */
public class Journal implements Serializable {

    private ArrayList<JournalEntry> entries;

    private ArrayList<Account> accounts;

	private ArrayList<Portfolio> portfolios;

    private String name;

    private String owner;

    private Date creationDate;

    private Date lastUpdateDate;

    public Journal(String name, String owner) {
        entries = new ArrayList<JournalEntry>(1000);
        accounts = new ArrayList<Account>(10);
        portfolios = new ArrayList<Portfolio>(25);

        setName(name);
        setOwner(owner);
        setCreationDate(new Date());
        setLastUpdateDate(new Date());

	    addPortfolio(new Portfolio("Portfel globalny"));
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public void addPortfolio(Portfolio portfolio) {
        portfolios.add(portfolio);
    }

    public void addEntry(JournalEntry entry) {
        entries.add(entry);
    }

	public ArrayList<JournalEntry> getEntries() {
		return entries;
	}

	public ArrayList<Account> getAccounts() {
		return accounts;
	}

	public ArrayList<Portfolio> getPortfolios() {
		return portfolios;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

}
