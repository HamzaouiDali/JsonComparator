import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.javacrumbs.jsonunit.core.Option

import java.util.regex.Pattern

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import static net.javacrumbs.jsonunit.JsonAssert.when

@groovy.util.logging.Commons
final class Compare {


    static enum ignoring {
        IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_EXTRA_FIELDS, IGNORING_VALUES
    }


    String response
    String expectedResponse
    boolean IGNORING_ARRAY_ORDER
    boolean IGNORING_EXTRA_ARRAY_ITEMS
    boolean IGNORING_EXTRA_FIELDS
    boolean IGNORING_VALUES

    def methodMissing(String methodName, args) {
        //  def section = new Section(title: methodName, body: args[0])
        //sections << section
        println(methodName + "does not exist")
    }


    def iGNORING_ARRAY_ORDER(boolean bool) {
        this.IGNORING_ARRAY_ORDER = bool
    }

    def iGNORING_EXTRA_ARRAY_ITEMS(boolean bool) {
        this.IGNORING_EXTRA_ARRAY_ITEMS = bool
    }

    def iGNORING_EXTRA_FIELDS(boolean bool) {
        this.IGNORING_EXTRA_FIELDS = bool
    }

    def iGNORING_VALUES(boolean bool) {
        this.IGNORING_VALUES = bool
    }


    def static compare(closure) {


        Compare tryTo = new Compare()
        // any method called in closure will be delegated to the tryTo class
        closure.delegate = tryTo
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
    }


    def result(String toText) {

        response = toText

    }


    def expectedResult(String expectedInput) {
        expectedResponse = expectedInput
    }


    def findAndDeleteKey(String key, def json) {

        if (json instanceof Map) {

            json.remove(key)

            for (def subkey : json.keySet()) {

                findAndDeleteKey(key, json.get(subkey))

            }

        } else if (json instanceof List) {

            for (def element : json.iterator()) {

                findAndDeleteKey(key, element)

            }

        }
        return json

    }


    def ignoringFields(String[] fields) throws Exception {
        try {
            def jsonSlurper = new JsonSlurper()


            def responseObject = jsonSlurper.parseText(this.response)
            def expectedResponsObject = jsonSlurper.parseText(this.expectedResponse)



            fields.each {
                (findAndDeleteKey("$it", responseObject))
                (findAndDeleteKey("$it", expectedResponsObject))


            }

            this.response = JsonOutput.prettyPrint(JsonOutput.toJson(responseObject))
            this.expectedResponse = JsonOutput.prettyPrint(JsonOutput.toJson(expectedResponsObject))

        } catch (groovy.json.JsonException e) {

            throw new MyException("La reponse actuelle(ou attendu) n'est pas un json valide")

        }
    }


    def getcomparePlease() throws Exception {
        try {
            comparePleaseThisObject(this)
        } catch (groovy.json.JsonException e) {

            throw new MyException("La reponse actuelle(ou attendu) n'est pas un json valide")

        }
        catch (java.lang.AssertionError e) {


            String error = e.getMessage()
            String errormessageTranslation = ""

            errormessageTranslation += error.replaceAll(Pattern.quote('JSON documents are different:'), '').replaceAll('but was', 'mais trouvee').replaceAll('Different keys found in node', '- Incoherence des libellés des noeuds : ').replaceAll('Different keys found in node "".', '- Valeurs des cles incoherentes :').replaceAll('Missing', 'Abscente').replaceAll('Different value found in node', '- Une incoherence trouvee dans le noeud').replaceAll('expected', 'Attendue').replaceAll('got', 'trouvee') + '\n'



            throw new MyException(errormessageTranslation)
        }

    }


    def static comparePleaseThisObject(Compare tryTo) {


        EnumSet<Option> opptionToIgnore = EnumSet.noneOf(net.javacrumbs.jsonunit.core.Option.class)

        ignoring.values().each { value ->
            if (tryTo."$value" == true) {
                opptionToIgnore.add net.javacrumbs.jsonunit.core.Option."$value"
            }

        }



        if (opptionToIgnore.size() > 1) {
            assertJsonEquals(tryTo.response, tryTo.expectedResponse, when(opptionToIgnore?.first(), opptionToIgnore?.last()))
        } else if (opptionToIgnore.size() == 1) {

            assertJsonEquals(tryTo.response, tryTo.expectedResponse, when(opptionToIgnore?.first()))
        } else {

            assertJsonEquals(tryTo.response, tryTo.expectedResponse)


        }

    }


}

//log.info context.testSuite.testStep.testStep.name

