package be.jdevelopment.tailrec.lib.processor;

import be.jdevelopment.tailrec.lib.strategy.TailRecursive;
import be.jdevelopment.tailrec.lib.threading.TailRecursiveExecutor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.*;
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
                           RoundEnvironment roundEnv
    ) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements
                    = roundEnv.getElementsAnnotatedWith(annotation);

            for (Element annotatedElement : annotatedElements) {
                Map<String, String> mapping = inferMapping(annotatedElement);
                byte[] content = fileContent(mapping);
                safeCreateClassFile(content, mapping.get("package_name"), mapping.get("engine_name"));
            }
        }

        return true;
    }

    private Map<String,String> inferMapping(Element element) {
        assert element.getKind() == ElementKind.CLASS;
        assert element instanceof TypeElement;
        TypeElement clz = (TypeElement) element;

        String engineName = clz.getAnnotation(TailRecDirective.class).name();

        ExecutableElement tailRecursiveMethod = clz.getEnclosedElements().stream()
                .filter($ -> $.getKind() == ElementKind.METHOD)
                .filter($ -> $.getAnnotationsByType(TailRecursive.class).length > 0)
                .map($ -> (ExecutableElement) $)
                .findAny()
                .orElseThrow();

        List<? extends VariableElement> tailRecursiveParameters = tailRecursiveMethod.getParameters();

        ExecutableElement tailExecutorMethod = clz.getEnclosedElements().stream()
                .filter($ -> $.getKind() == ElementKind.METHOD)
                .filter($ -> $.getAnnotationsByType(TailRecursiveExecutor.class).length > 0)
                .map($ -> (ExecutableElement) $)
                .findAny()
                .orElseThrow();

        List<? extends VariableElement> tailExecutorParameters = tailExecutorMethod.getParameters();
        List<? extends TypeMirror> tailExecutorThrowTypes = tailExecutorMethod.getThrownTypes();

        Map<String, Object> mapping = new HashMap<>();
        mapping.put("package_name", elementUtils.getPackageOf(element));
        mapping.put("directive_name", clz.getSimpleName());
        mapping.put("engine_name", engineName);
        mapping.put("tail_rec_name", tailRecursiveMethod.getSimpleName());
        mapping.put("str:tail_rec_name", '"' + tailRecursiveMethod.getSimpleName().toString() + '"');
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

        return mapping.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
    }

    private byte[] fileContent(Map<String, String> translator) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (
                InputStream inputStream = getClass().getClassLoader()
                        .getResourceAsStream("tail_rec_engine_template.txt");
                Scanner scanner = new Scanner(inputStream)
        ) {

            Pattern pattern = Pattern.compile("^\\$\\{(str:)?[a-zA-Z_]+}$");
            String word;
            while (scanner.hasNext()) {
                word = scanner.next();
                if (pattern.matcher(word).matches()) {
                    word = word.substring(0, word.length() - 1).substring(2);
                    word = translator.get(word);

                    output.writeBytes(word.getBytes());
                    output.write(' ');
                    continue;
                }
                output.writeBytes(word.getBytes());
                output.write(' ');
            }

        }
        catch (IOException e) {
            throw new Error(e);
        }

        return output.toByteArray();
    }

    private void safeCreateClassFile(byte[] fileContent, String packageName, String className) {
        try {
            createClassFile(fileContent, packageName, className);
        } catch(IOException e) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            throw new Error(e);
        }
    }

    private void createClassFile(byte[] fileContent, String packageName, String className) throws IOException {
        JavaFileObject fileObject = processingEnvironment.getFiler()
                .createSourceFile(packageName + "." + className);
        try(PrintWriter outputStream = new PrintWriter(fileObject.openWriter())) {
            ByteArrayInputStream input = new ByteArrayInputStream(fileContent);
            int r;
            while ((r = input.read()) != -1)
                outputStream.write(r);
        }
    }

}
