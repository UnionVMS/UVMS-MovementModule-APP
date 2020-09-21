package eu.europa.ec.fisheries.uvms.movement.model;

import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="movement" type="{urn:movement.schema.fisheries.ec.europa.eu:v1}MovementType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="totalNumberOfPages" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *         &lt;element name="currentPage" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "movement",
        "totalNumberOfPages",
        "currentPage"
})
@XmlRootElement(name = "getMovementListByQueryResponse")
public class GetMovementListByQueryResponse
        implements Serializable
{

    //Moved out of the wsdl files since they, by standard, does not generate setters for list et al and jsonB really needs setters to work

    private final static long serialVersionUID = 1L;
    protected List<MovementType> movement;
    @XmlElement(required = true)
    protected BigInteger totalNumberOfPages;
    @XmlElement(required = true)
    protected BigInteger currentPage;

    /**
     * Gets the value of the movement property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the movement property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMovement().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MovementType }
     *
     *
     */
    public List<MovementType> getMovement() {
        if (movement == null) {
            movement = new ArrayList<MovementType>();
        }
        return this.movement;
    }

    public void setMovement(List<MovementType> movement) {
        this.movement = movement;
    }

    /**
     * Gets the value of the totalNumberOfPages property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getTotalNumberOfPages() {
        return totalNumberOfPages;
    }

    /**
     * Sets the value of the totalNumberOfPages property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setTotalNumberOfPages(BigInteger value) {
        this.totalNumberOfPages = value;
    }

    /**
     * Gets the value of the currentPage property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets the value of the currentPage property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setCurrentPage(BigInteger value) {
        this.currentPage = value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}

