package pl.com.bottega.ecommerce.sales.domain.invoicing;

import java.util.Date;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.*;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import static org.mockito.Mockito.*;

public class BookKeeperTest {

	private BookKeeper bookKeeper;

	@Test
	public void issuance_requestInvoiceWithOnePosition_shouldReturnInvoiceWithOnePosition() {

		// given
		Id id = new Id("1");
		Money money = new Money(1);
		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		ClientData clientData = new ClientDataBuilder().build();
		when(mockInvoiceFactory.create(clientData)).thenReturn(
				new Invoice(id, clientData));
		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax(ProductType.FOOD, money)).thenReturn(
				new Tax(money, "opis"));
		ProductData productData = new ProductDataBuilder().withPrice(1).withProductType(ProductType.FOOD).build();
		RequestItem requestItem = new RequestItemBuilder().withProductData(productData).witTotalCost(1).build();
		invoiceRequest.add(requestItem);

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);
		int result = invoiceResult.getItems().size();

		// then
		assertThat(result, is(1));
	}

	@Test
	public void issuance_twoPositionInvoiceRequest_callCalculateTaxTwice() {

		// given
		Money moneyEveryItem = new Money(1);
		ProductType productTypeEveryItem = ProductType.FOOD;
		ClientData clientData = new ClientDataBuilder().build();
		ProductData productData = new ProductDataBuilder().withPrice(1).withProductType(ProductType.FOOD).build();
		RequestItem requestItem = new RequestItemBuilder().withProductData(productData).witTotalCost(1).build();
		
		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		when( mockInvoiceFactory.create( (ClientData) Mockito.any() ) ).thenReturn( new Invoice( Id.generate() , clientData ) );
		TaxPolicy taxPolicy = mock( TaxPolicy.class );
		when( taxPolicy.calculateTax( (ProductType)Mockito.any(), (Money)Mockito.any() ) ).thenReturn( new Tax( new Money( 0 ) , "" ) ); 

		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
		invoiceRequest.add(requestItem);
		invoiceRequest.add(requestItem);

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);

		// then
		Mockito.verify(taxPolicy, Mockito.times(2)).calculateTax(
				productTypeEveryItem, moneyEveryItem);
	}

	@Test
	public void issuance_requestInvoiceWithNoPosition_shouldReturnInvoiceWithNoPosition() {

		// given
		Id id = new Id("1");
		Money money = new Money(1);
		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		ClientData clientData = new ClientDataBuilder().build();
		when(mockInvoiceFactory.create(clientData)).thenReturn(
				new Invoice(id, clientData));
		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax(ProductType.FOOD, money)).thenReturn(
				new Tax(money, "opis"));
		ProductData productData = new ProductDataBuilder().withPrice(1).withProductType(ProductType.FOOD).build();

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);
		int result = invoiceResult.getItems().size();

		// then
		assertThat(result, is(0));
	}

	@Test
	public void issuance_NoPositionInvoiceRequest_notCallCalculateTax() {

		// given
		Id id = new Id("1");
		Money moneyEveryItem = new Money(1);
		ProductType productTypeEveryItem = ProductType.FOOD;
		ClientData clientData = new ClientDataBuilder().build();
		ProductData productData = new ProductDataBuilder().withPrice(1).withProductType(ProductType.FOOD).build();
		RequestItem requestItem = new RequestItemBuilder().withProductData(productData).witTotalCost(1).build();

		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		when(mockInvoiceFactory.create(clientData)).thenReturn(
				new Invoice(id, clientData));
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax(productTypeEveryItem, moneyEveryItem))
				.thenReturn(new Tax(moneyEveryItem, "opis"));

		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);

		// then
		Mockito.verify(taxPolicy, Mockito.times(0)).calculateTax(
				productTypeEveryItem, moneyEveryItem);
	}
}
