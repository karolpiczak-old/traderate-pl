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

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JournalFixture {

    @Test
    public void shouldSerialize() throws Exception {
        try {
            Journal journal = new Journal("Secret trade journal", "John Doe");

            FileOutputStream fos = new FileOutputStream("journal.dat.test");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(journal);
            oos.close();
        } catch (Exception ex) {
            fail("Exception thrown during test: " + ex.toString());
        }

        try {
            FileInputStream fis = new FileInputStream("journal.dat.test");
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            Journal journal = (Journal) ois.readObject();
            ois.close();
            
            assertEquals("Secret trade journal", journal.getName());
            assertEquals("John Doe", journal.getOwner());
            
            new File("journal.dat.test").delete();
        } catch (Exception ex) {
            fail("Exception thrown during test: " + ex.toString());
        }  
        
    }
}
