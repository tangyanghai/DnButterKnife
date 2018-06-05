package com.example.butterknife_compiler;

import com.example.butterknife_annotation.BindView;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

/**
 * @author : Administrator
 * @time : 15:28
 * @for :
 */

@AutoService(Processor.class)
public class ButterKnifeProcesser extends AbstractProcessor {

    /**
     * @return 支持属性
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * @param set              属性集合
     * @param roundEnvironment 环境
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        System.out.print("---------------------process-----------------");

        //所有包含BindView注解的成员变量集合
        Set<? extends Element> elementsAnnotatedSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //为集合分类,KEY-->全类名,value-->成员变量
        Map<String, List<VariableElement>> cacheMap = sortElement(elementsAnnotatedSet);
        //为每一个类创建副本
        Iterator<String> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            //类名
            String activityName = iterator.next();
            //成员变量集合
            List<VariableElement> list = cacheMap.get(activityName);
            //创建类
            createClass(activityName, list);
        }

        return false;
    }

    /**
     * 创建副本类型
     *
     * @param activityName
     * @param elements
     */
    private void createClass(String activityName, List<VariableElement> elements) {
        //副本类名
        String className = activityName + "$ViewBinder";
        Filer filer = processingEnv.getFiler();
        try {
            //java类
            JavaFileObject sourceFile = filer.createSourceFile(className);

            String packageName = getPackageName(elements.get(0));
            //相当于工人
            Writer writer = sourceFile.openWriter();
            //   caheElements.get(0)  VariableElement            caheElements.get(0).getEnclosingElement().   TypeElement  MainActivity$ViewBinder
            String activitySimpleName = elements.get(0).getEnclosingElement().getSimpleName().toString() + "$ViewBinder";

            //头部分
            writeHeader(writer, packageName, activityName, activitySimpleName);

            //中间部分
            for (VariableElement variableElement : elements) {
                BindView bindView = variableElement.getAnnotation(BindView.class);
                int id = bindView.value();
                String fieldName = variableElement.getSimpleName().toString();
                TypeMirror typeMirror = variableElement.asType();
                //  TextView
                writer.write("target." + fieldName + "=(" + typeMirror.toString() + ")target.findViewById(" + id + ");");
                writer.write("\n");
            }

            //结尾部分
            writer.write("\n");
            writer.write("}");
            writer.write("\n");
            writer.write("}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 拼装类的头部
     */
    private void writeHeader(Writer writer, String packageName, String activityName, String activitySimpleName) {
        try {
            writer.write("package " + packageName + ";");
            writer.write("\n");
            writer.write("import com.example.butterknife_interface.ViewBinder;");
            writer.write("\n");

            writer.write("public class " + activitySimpleName +
                    " implements  ViewBinder<" + activityName + "> {");

            writer.write("\n");
            writer.write(" public void bind( " + activityName + " target) {");
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param elementsAnnotatedSet 将成员变量集合分类
     */
    private Map<String, List<VariableElement>> sortElement(Set<? extends Element> elementsAnnotatedSet) {
        Map<String, List<VariableElement>> cacheMap = new HashMap<>();
        Iterator<? extends Element> iterator = elementsAnnotatedSet.iterator();
        while (iterator.hasNext()) {
            //成员变量
            VariableElement variableElement = (VariableElement) iterator.next();
            //全类名
            String fullName = getFullName(variableElement);
            List<VariableElement> variableElements;
            if (cacheMap.containsKey(fullName)) {
                variableElements = cacheMap.get(fullName);
                variableElements.add(variableElement);
            } else {
                variableElements = new ArrayList<>();
                variableElements.add(variableElement);
                cacheMap.put(fullName, variableElements);
            }
        }
        return cacheMap;
    }

    /**
     * 通过成员变量,获取全类名
     * @param variableElement 成员变量
     * @return 全类名
     */
    private String getFullName(VariableElement variableElement) {
        Element enclosingElement = variableElement.getEnclosingElement();
        String packageName = getPackageName(variableElement);
        return packageName + "." + enclosingElement.getSimpleName().toString();
    }

    /**
     * 通过成员变量,获取包名
     * @param variableElement 成员变量
     * @return 包名
     */
    private String getPackageName(VariableElement variableElement) {
        Element enclosingElement = variableElement.getEnclosingElement();
        String packageName = processingEnv.getElementUtils().getPackageOf(enclosingElement).getQualifiedName().toString();
        return packageName;
    }
}
