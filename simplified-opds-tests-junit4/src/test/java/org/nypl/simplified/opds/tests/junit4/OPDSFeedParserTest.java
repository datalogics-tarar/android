package org.nypl.simplified.opds.tests.junit4;

import org.junit.Test;
import org.nypl.simplified.opds.tests.contracts.OPDSFeedParserContract;
import org.nypl.simplified.opds.tests.contracts.OPDSFeedParserContractType;

public final class OPDSFeedParserTest implements OPDSFeedParserContractType
{
  private final OPDSFeedParserContractType contract;

  public OPDSFeedParserTest()
  {
    this.contract = new OPDSFeedParserContract();
  }

  @Override @Test public void testAcquisitionFeedCategories0()
    throws Exception
  {
    this.contract.testAcquisitionFeedCategories0();
  }

  @Override @Test public void testAcquisitionFeedFacets0()
    throws Exception
  {
    this.contract.testAcquisitionFeedFacets0();
  }

  @Override @Test public void testAcquisitionFeedFiction0()
    throws Exception
  {
    this.contract.testAcquisitionFeedFiction0();
  }

  @Override @Test public void testAcquisitionFeedGroups0()
    throws Exception
  {
    this.contract.testAcquisitionFeedGroups0();
  }

  @Override @Test public void testAcquisitionFeedPaginated0()
    throws Exception
  {
    this.contract.testAcquisitionFeedPaginated0();
  }

  @Override @Test public void testDOMException()
    throws Exception
  {
    this.contract.testDOMException();
  }

  @Override @Test public void testEmpty0()
    throws Exception
  {
    this.contract.testEmpty0();
  }

  @Override public void testEntryAsFeed0()
    throws Exception
  {
    this.contract.testEntryAsFeed0();
  }

  @Override @Test public void testNotXMLException()
    throws Exception
  {
    this.contract.testNotXMLException();
  }

  @Override @Test public void testParserURISyntaxException()
    throws Exception
  {
    this.contract.testParserURISyntaxException();
  }

  @Override @Test public void testStreamIOException()
    throws Exception
  {
    this.contract.testStreamIOException();
  }
}
