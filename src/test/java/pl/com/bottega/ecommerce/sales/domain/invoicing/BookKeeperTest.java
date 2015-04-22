package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.junit.Assert.*;

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
		ClientData clientData = new ClientDataBuilder().build();
		ProductData productData = new ProductDataBuilder().withPrice(1)
				.withProductType(ProductType.FOOD).build();
		RequestItem requestItem = new RequestItemBuilder()
				.withProductData(productData).witTotalCost(1).build();
		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
		invoiceRequest.add(requestItem);

		// mocks
		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		when(mockInvoiceFactory.create(clientData)).thenReturn(
				new Invoice(Id.generate(), clientData));
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax((ProductType) any(), (Money) any()))
				.thenReturn(new Tax(new Money(0), ""));

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);
		int result = invoiceResult.getItems().size();

		// then
		assertThat(result, is(1));
	}

	@Test
	public void issuance_twoPositionInvoiceRequest_callCalculateTaxTwice() {

		// given
		ClientData clientData = new ClientDataBuilder().build();
		ProductData productData = new ProductDataBuilder().withPrice(1)
				.withProductType(ProductType.FOOD).build();
		RequestItem requestItem = new RequestItemBuilder()
				.withProductData(productData).witTotalCost(1).build();

		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		when(mockInvoiceFactory.create((ClientData) any())).thenReturn(
				new Invoice(Id.generate(), clientData));
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax((ProductType) any(), (Money) any()))
				.thenReturn(new Tax(new Money(0), ""));

		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
		invoiceRequest.add(requestItem);
		invoiceRequest.add(requestItem);

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);

		// then
		Mockito.verify(taxPolicy, Mockito.times(2)).calculateTax(
				(ProductType) any(), (Money) any());
	}

	@Test
	public void issuance_requestInvoiceWithNoPosition_shouldReturnInvoiceWithNoPosition() {

		// given
		ClientData clientData = new ClientDataBuilder().build();
		ProductData productData = new ProductDataBuilder().withPrice(1)
				.withProductType(ProductType.FOOD).build();
		RequestItem requestItem = new RequestItemBuilder()
				.withProductData(productData).witTotalCost(1).build();
		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

		// mocks
		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		when(mockInvoiceFactory.create(clientData)).thenReturn(
				new Invoice(Id.generate(), clientData));
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax((ProductType) any(), (Money) any()))
				.thenReturn(new Tax(new Money(0), ""));

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);
		int result = invoiceResult.getItems().size();

		// then
		assertThat(result, is(0));
	}

	@Test
	public void issuance_NoPositionInvoiceRequest_notCallCalculateTax() {

		// given
		ClientData clientData = new ClientDataBuilder().build();
		ProductData productData = new ProductDataBuilder().withPrice(1)
				.withProductType(ProductType.FOOD).build();
		RequestItem requestItem = new RequestItemBuilder()
				.withProductData(productData).witTotalCost(1).build();
		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

		// mocks
		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		when(mockInvoiceFactory.create(clientData)).thenReturn(
				new Invoice(Id.generate(), clientData));
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax((ProductType) any(), (Money) any()))
				.thenReturn(new Tax(new Money(0), ""));

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);

		// then
		Mockito.verify(taxPolicy, Mockito.times(0)).calculateTax(
				(ProductType) any(), (Money) any());
	}
}
