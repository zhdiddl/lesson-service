package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.QLesson;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
@RequiredArgsConstructor
@Repository
public class LessonRepositoryCustomImpl implements LessonRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Lesson> searchLessonsByTitle(String title, int offset, int limit) {
        QLesson lesson = QLesson.lesson;

        // where 조건을 사용해 title에 대한 like 검색
        BooleanExpression predicate = lesson.title.like("%" + title + "%");

        return queryFactory.selectFrom(lesson)
                .where(predicate)
                .offset(offset)  // 가져올 시작점 설정
                .limit(limit)    // 한 번에 가져올 개수 설정
                .fetch();        // 가져오기 수행
    }
}

