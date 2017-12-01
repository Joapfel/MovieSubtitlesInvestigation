

/* First created by JCasGen Sun Nov 26 00:17:13 CET 2017 */
package de.ws1718.ismla.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sun Nov 26 00:17:13 CET 2017
 * XML source: /home/johannes/workspace_industrial_strength/MovieSubtitlesInvestigation/src/resources/typeSystemDescriptor.xml
 * @generated */
public class Compound extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Compound.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Compound() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Compound(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Compound(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Compound(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: rank

  /** getter for rank - gets 
   * @generated
   * @return value of the feature 
   */
  public int getRank() {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_rank == null)
      jcasType.jcas.throwFeatMissing("rank", "de.ws1718.ismla.types.Compound");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Compound_Type)jcasType).casFeatCode_rank);}
    
  /** setter for rank - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRank(int v) {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_rank == null)
      jcasType.jcas.throwFeatMissing("rank", "de.ws1718.ismla.types.Compound");
    jcasType.ll_cas.ll_setIntValue(addr, ((Compound_Type)jcasType).casFeatCode_rank, v);}    
  }

    