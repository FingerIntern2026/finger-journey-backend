// ReportStatus
// AiReport.status(varchar)에 실제로 들어가는 값을 코드에서 오타 없이 다루기 위한 enum
// DB 컬럼 자체는 ERD대로 varchar(20)이고, 저장/조회 시 .name()/valueOf()로 변환해서 씀

package com.finger.fingerjourneybackend.entity;

public enum ReportStatus {
    GENERATING,
    COMPLETED,
    FAILED
}
