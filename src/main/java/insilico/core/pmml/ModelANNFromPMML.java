package insilico.core.pmml;

import insilico.core.exception.InitFailureException;
import insilico.core.localization.StringSelectorCore;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jpmml.model.PMMLUtil.unmarshal;

/**
 * Wrapper for running simple ANN model encoded in a PMML file
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ModelANNFromPMML {

    private final Evaluator evaluator;
    private final FieldName outputField;
    protected boolean verbose;


    public ModelANNFromPMML(InputStream PmmlSource, String outputField) throws InitFailureException {

        try {

            // Load the model from the given resource (PMML file)
            PMML pmml = unmarshal(PmmlSource);

            // Create the evaluator object
            ModelEvaluatorBuilder modelEvaluatorBuilder = new ModelEvaluatorBuilder(pmml);
            //            ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
            evaluator = modelEvaluatorBuilder.build();
//            Evaluator evaluator = (Evaluator)modelEvaluatorFactory.newModelEvaluator()

        } catch (Exception e) {
            throw new InitFailureException(String.format(StringSelectorCore.getString("pmml_ann_unable_init_pmml_model"), e.getMessage()));
        }

        this.outputField = FieldName.create(outputField);
        this.verbose = false;
    }


    // Run the model using the descriptors, provided as a Map with
    // Key: Descriptor name
    // Value: Descriptor value
    public double Evaluate(Map<String, Object> Descriptors) throws Exception {

        // Prepare arguments for the evaluator
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();
        List<InputField> inputFields = evaluator.getInputFields();

        for(InputField inputField : inputFields){
            FieldName inputFieldName = inputField.getName();

            // Check if descriptor is available
            if (!Descriptors.containsKey(inputField.getName().getValue()))
                throw new Exception(String.format(StringSelectorCore.getString("pmml_ann_descriptor_not_found"),inputField.getName().getValue() ));

            // The raw (ie. user-supplied) value could be any Java primitive value
            Object rawValue = Descriptors.get(inputField.getName().getValue());
            if (verbose)
                System.out.println(inputField.getName().getValue() + " : " + Descriptors.get(inputField.getName().getValue()));

            // The raw value is passed through: 1) outlier treatment, 2) missing value treatment, 3) invalid value treatment and 4) type conversion
            FieldValue inputFieldValue = inputField.prepare(rawValue);

            arguments.put(inputFieldName, inputFieldValue);
        }

        // Evaluate model
        Map<FieldName, ?> outputs = evaluator.evaluate(arguments);


        try {
            return (Double)(outputs.get(outputField));
        } catch (Exception ex) {

            String result = outputs.values().toArray()[0].toString();
            if(result.contains("{result=")) {
                String value = result.substring(8, result.lastIndexOf("}"));
                return Double.parseDouble(value);
            } else {
                throw new Exception(ex.getMessage()); }

        }

    }

    // Run the model using the descriptors, provided as a Map with
    // Key: Descriptor name
    // Value: Descriptor value
    public Map<FieldName, ?> EvaluateFullOutput(Map<String, Object> Descriptors) throws Exception {

        // Prepare arguments for the evaluator
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();
        List<InputField> inputFields = evaluator.getInputFields();

        for(InputField inputField : inputFields){
            FieldName inputFieldName = inputField.getName();

            // Check if descriptor is available
            if (!Descriptors.containsKey(inputField.getName().getValue()))
                throw new Exception(String.format(StringSelectorCore.getString("pmml_ann_descriptor_not_found"),inputField.getName().getValue() ));

            // The raw (ie. user-supplied) value could be any Java primitive value
            Object rawValue = Descriptors.get(inputField.getName().getValue());
            if (verbose)
                System.out.println(inputField.getName().getValue() + " : " + Descriptors.get(inputField.getName().getValue()));

            // The raw value is passed through: 1) outlier treatment, 2) missing value treatment, 3) invalid value treatment and 4) type conversion
            FieldValue inputFieldValue = inputField.prepare(rawValue);

            arguments.put(inputFieldName, inputFieldValue);
        }

        // Evaluate model
        return evaluator.evaluate(arguments);

    }


    /**
     * @return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }


    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
