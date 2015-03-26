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
	public void requestInvoiceWithOnePosition_shouldReturnInvoiceWithOnePosition() {

		// given
		Id id = new Id("1");
		Money money = new Money(1);
		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		ClientData clientData = new ClientData(id, "Arek");
		when(mockInvoiceFactory.create(clientData)).thenReturn(
				new Invoice(id, clientData));
		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax(ProductType.FOOD, money)).thenReturn(
				new Tax(money, "opis"));
		ProductData productData = new ProductData(id, money, "ksiazka",
				ProductType.FOOD, new Date());
		RequestItem requestItem = new RequestItem(productData, 4, money);
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
		Id id = new Id("1");
		Money moneyEveryItem = new Money(1);
		ProductType productTypeEveryItem = ProductType.FOOD;
		ClientData clientData = new ClientData(id, "Arek");
		ProductData productData = new ProductData(id, moneyEveryItem,
				"ksiazka", productTypeEveryItem, new Date());
		RequestItem requestItem = new RequestItem(productData, 4,
				moneyEveryItem);

		InvoiceFactory mockInvoiceFactory = mock(InvoiceFactory.class);
		bookKeeper = new BookKeeper(mockInvoiceFactory);
		when(mockInvoiceFactory.create(clientData)).thenReturn(
				new Invoice(id, clientData));
		TaxPolicy taxPolicy = mock(TaxPolicy.class);
		when(taxPolicy.calculateTax(productTypeEveryItem, moneyEveryItem))
				.thenReturn(new Tax(moneyEveryItem, "opis"));

		InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
		invoiceRequest.add(requestItem);
		invoiceRequest.add(requestItem);

		// when
		Invoice invoiceResult = bookKeeper.issuance(invoiceRequest, taxPolicy);

		// then
		Mockito.verify(taxPolicy, Mockito.times(2)).calculateTax(
				productTypeEveryItem, moneyEveryItem);
	}
}
