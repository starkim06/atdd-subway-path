package nextstep.subway.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nextstep.subway.domain.Line;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

class LineTest {

    private static final Station 강남역 = new Station("강남역");
    private static final Station 양재역 = new Station("양재역");
    private static final Station 신사역 = new Station("신사역");
    private static final Station 잠실역 = new Station("잠실역");

    @Nested
    @DisplayName("구간을 등록할 때")
    class AddSectionTest {
        @DisplayName("구간 목록 마지막에 새로운 구간을 등록할 경우")
        @Test
        void addSection() {
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);
            line.registerSection(양재역, 신사역, 100);

            assertThat(line.getSections()).hasSize(2);
        }

        @DisplayName("구간 목록 사이에 새로운 구간을 등록할 경우")
        @Test
        void addSection2() {
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);
            line.registerSection(강남역, 신사역, 50);

            assertThat(line.getSections()).hasSize(2);
        }

        @DisplayName("추가하는 구간의 상행역과 하행역이 모두 기존 노선에 포함되어 있는 경우")
        @Test
        void addSection3() {
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);

            assertThrows(IllegalArgumentException.class, () -> line.registerSection(강남역, 양재역, 50));
        }

        @DisplayName("추가하는 구간의 상행역과 하행역이 모두 기존 노선에 포함되어 있지 않은 경우")
        @Test
        void addSection4() {
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);

            assertThrows(IllegalArgumentException.class, () -> line.registerSection(신사역, 잠실역, 100));
        }

        @DisplayName("구간 목록 사이에 새로운 구간을 등록할 때 새로운 구간의 길이가 기존의 구간보다 같거나 긴 경우")
        @ParameterizedTest(name = "기존 구간의 길이 {0} , 새로운 구간의 길이 {1}")
        @CsvSource(value = {
                "100, 100",
                "100, 101",
                "100, 100000",
        })
        void addSection5(int existingDistance, int newDistance) {
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, existingDistance);

            assertThrows(IllegalArgumentException.class, () -> line.registerSection(강남역, 신사역, newDistance));
        }
    }

    @DisplayName("노선에 속해있는 역 목록 조회")
    @Test
    void getStations() {
        Line line = new Line("2호선", "빨간색");
        line.registerSection(강남역, 양재역, 100);
        line.registerSection(강남역, 신사역, 50);

        List<Section> sections = line.getSections();
        assertThat(sections).extracting(Section::getUpStation).extracting(Station::getName).containsExactly("강남역", "신사역");
        assertThat(sections).extracting(Section::getDownStation).extracting(Station::getName).containsExactly("신사역", "양재역");
    }

    @Nested
    @DisplayName("구간을 삭제할 때")
    class RemoveSectionTest {
        @DisplayName("구간 목록에서 마지막 역 삭제")
        @Test
        void removeSection() {
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);
            line.registerSection(양재역, 신사역, 100);

            line.deleteSection(신사역);
            List<Section> sections = line.getSections();
            assertThat(sections).hasSize(1);
            assertThat(sections).extracting(Section::getUpStation).extracting(Station::getName).containsExactly("강남역");
        }

        @DisplayName("구간 목록에서 상행종점역 삭제")
        @Test
        void removeSection2() {
            //given
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);
            line.registerSection(양재역, 신사역, 100);

            //when
            line.deleteSection(강남역);

            //then
            List<Section> sections = line.getSections();
            assertThat(sections).hasSize(1);
            assertThat(sections).extracting(Section::getUpStation).extracting(Station::getName).containsExactly("양재역");
            assertThat(sections).extracting(Section::getDownStation).extracting(Station::getName).containsExactly("신사역");
        }

        @DisplayName("구간 목록에서 가운대역 삭제")
        @Test
        void removeSection3() {
            //given
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);
            line.registerSection(양재역, 신사역, 100);

            //when
            line.deleteSection(양재역);

            //then
            List<Section> sections = line.getSections();
            assertThat(sections).hasSize(1);
            assertAll(
                    () -> assertThat(sections.get(0).getDistance()).isEqualTo(200),
                    () -> assertThat(sections.get(0).getUpStation()).isEqualTo(강남역),
                    () -> assertThat(sections.get(0).getDownStation()).isEqualTo(신사역)
            );
        }

        @DisplayName("구간이 하나인 노선에선 구간 제거가 안된다.")
        @Test
        void removeSection4() {
            //given
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);

            //when, then
            assertThrows(IllegalArgumentException.class, () -> line.deleteSection(양재역));
        }

        @DisplayName("구간에 포함되지 않은 역은 제거가 안된다.")
        @Test
        void removeSection5() {
            //given
            Line line = new Line("2호선", "빨간색");
            line.registerSection(강남역, 양재역, 100);
            line.registerSection(양재역, 잠실역, 100);

            //when, then
            assertThrows(IllegalArgumentException.class, () -> line.deleteSection(신사역));
        }
    }
}
