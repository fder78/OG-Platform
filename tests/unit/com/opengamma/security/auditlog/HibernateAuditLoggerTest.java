/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Collection;

import org.junit.Test;

import com.opengamma.util.test.HibernateTest;

/**
 * 
 *
 * @author pietari
 */
public class HibernateAuditLoggerTest extends HibernateTest {
  
  @Override
  public String getConfigLocation() {
    return "com/opengamma/security/auditlog/auditlog-testing-context.xml";
  }  
  
  @Test
  public void testLogging() throws Exception {
    HibernateAuditLogger logger = (HibernateAuditLogger) _context.getBean("myAuditLogger");
    logger.log("jake", "/Portfolio/XYZ123", "View", true);
    logger.log("jake", "/Portfolio/XYZ345", "Modify", "User has no Modify permission on this portfolio", false);
    
    // not flushed yet
    Collection<AuditLogEntry> logEntries = logger.findAll();
    assertEquals(0, logEntries.size()); 
    
    Thread.sleep(2000);
    
    // flushed
    logEntries = logger.findAll();
    assertEquals(2, logEntries.size()); 
    
    for (AuditLogEntry entry : logEntries) {
      assertEquals("jake", entry.getUser());

      if (entry.getObject().equals("/Portfolio/XYZ123")) {
        assertEquals("View", entry.getOperation());
        assertNull(entry.getDescription());
        assertTrue(entry.isSuccess());
      
      } else if (entry.getObject().equals("/Portfolio/XYZ345")) {
        assertEquals("Modify", entry.getOperation());
        assertEquals("User has no Modify permission on this portfolio", entry.getDescription());
        assertFalse(entry.isSuccess());
      
      } else {
        fail("Unexpected object ID");
      }
    }
    
  }
}
