package be.jdevelopment.tailrec.lib.processor;

import be.jdevelopment.tailrec.lib.strategy.TailRecursive;
import be.jdevelopment.tailrec.lib.threading.TailRecursiveExecutor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SupportedAnnotationTypes(
        "be.jdevelopment.tailrec.lib.processor.TailRecDirective")
@SupportedSourceVersion(SourceVersion.RELEASE_14)
public final class TailRecDirectiveProcessor extends AbstractProcessor {

    private ProcessingEnvironment processingEnvironment;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
        this.elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements
                    = roundEnv.getElementsAnnotatedWith(annotation);

            for (Element annotatedElement : annotatedElements)
                onDirectiveFound(annotatedElement);
        }

        return true;
    }

    private void onDirectiveFound(Element element) {
        assert element.getKind() == ElementKind.CLASS;
        assert element instanceof TypeElement;
        var clz = (TypeElement) element;
        assert element.getSimpleName().toString().endsWith("Directive");
        System.out.print("\n\n>> " + element);

        var tailRecursiveMethod = clz.getEnclosedElements().stream()
                .filter($ -> $.getKind() == ElementKind.METHOD)
                .filter($ -> $.getAnnotationsByType(TailRecursive.class).length > 0)
                .map($ -> (ExecutableElement) $)
                .findAny()
                .orElseThrow();

        var tailRecursiveParameters = tailRecursiveMethod.getParameters();

        var tailExecutorMethod = clz.getEnclosedElements().stream()
                .filter($ -> $.getKind() == ElementKind.METHOD)
                .filter($ -> $.getAnnotationsByType(TailRecursiveExecutor.class).length > 0)
                .map($ -> (ExecutableElement) $)
                .findAny()
                .orElseThrow();

        var tailExecutorParameters = tailExecutorMethod.getParameters();
        var tailExecutorThrowTypes = tailExecutorMethod.getThrownTypes();

        var mapping = new HashMap<String, Object>();
        mapping.put("package_name", elementUtils.getPackageOf(element));
        mapping.put("directive_name", clz.getSimpleName());
        mapping.put("engine_name", clz.getSimpleName().toString().substring(0, clz.getSimpleName().toString().length() - "Directive".length()));
        mapping.put("tail_rec_name", tailRecursiveMethod.getSimpleName());
        mapping.put("tail_executor_name", tailExecutorMethod.getSimpleName());
        mapping.put("tail_rec_unwrapped_array", IntStream.range(0, tailRecursiveParameters.size())
                .mapToObj(i -> String.format("(%s)args[%d]", tailRecursiveParameters.get(i).asType(), i))
                .collect(Collectors.joining(","))
        );
        mapping.put("tail_rec_args_signature", IntStream.range(0, tailRecursiveParameters.size())
                .mapToObj(i -> String.format("%s arg%d", tailRecursiveParameters.get(i).asType(), i))
                .collect(Collectors.joining(","))
        );
        mapping.put("tail_rec_args_name", IntStream.range(0, tailRecursiveParameters.size())
                .mapToObj(i -> String.format("arg%d", i))
                .collect(Collectors.joining(","))
        );
        mapping.put("tail_executor_return_type", tailExecutorMethod.getReturnType());
        mapping.put("tail_executor_args_signature", IntStream.range(0, tailExecutorParameters.size())
                .mapToObj(i -> String.format("%s arg%d", tailExecutorParameters.get(i).asType(), i))
                .collect(Collectors.joining(","))
        );
        mapping.put("tail_executor_args_name", IntStream.range(0, tailExecutorParameters.size())
                .mapToObj(i -> String.format("arg%d", i))
                .collect(Collectors.joining(","))
        );
        mapping.put("tail_executor_throw_types", tailExecutorThrowTypes.stream()
                .map(TypeMirror::toString)
                .collect(Collectors.joining(","))
        );
        mapping.put("executor_name", tailExecutorMethod.getAnnotation(TailRecursiveExecutor.class).executor());

        mapping.entrySet().stream()
                .map(entry -> String.format("\n\t%s: %s", entry.getKey(), entry.getValue()))
                .forEach(System.out::print);
        System.out.println("\n<<\n");

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tail_rec_engine_template.txt");
             Scanner scanner = new Scanner(inputStream)) {

            Pattern pattern = Pattern.compile("^\\$\\{[a-zA-Z_]+}$");
            String word;
            while (scanner.hasNext()) {
                word = scanner.next();
                if (pattern.matcher(word).matches()) {
                    word = word.substring(0, word.length() - 1).substring(2);
                    boolean stringify = "tail_rec_name".equals(word);
                    word = mapping.get(word).toString();
                    if (stringify)
                        word = '"' + word + '"';

                    System.out.println(word);
                    continue;
                }
                System.out.println(word);
            }

        }
        catch (IOException e) {
            throw new Error(e);
        }
    }

}
