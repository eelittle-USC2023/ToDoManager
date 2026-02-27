package com.example.todomanager.service;

import com.example.todomanager.model.TaskFolder;
import com.example.todomanager.model.User;
import com.example.todomanager.repository.TaskFolderRepository;
import com.example.todomanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TaskFolderService {

    private final TaskFolderRepository folderRepo;
    private final UserRepository userRepo;

    public TaskFolderService(TaskFolderRepository folderRepo, UserRepository userRepo) {
        this.folderRepo = folderRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public TaskFolder create(TaskFolder folder) {
        if (folder == null) throw new BusinessException("folder is null");
        if (folder.getUser() == null || folder.getUser().getId() == null) throw new BusinessException("folder must reference user id");
        User user = userRepo.findById(folder.getUser().getId()).orElseThrow(() -> new BusinessException("user not found"));
        // ensure unique title for user
        if (folderRepo.findByUserIdAndTitle(user.getId(), folder.getTitle()).isPresent()) {
            throw new BusinessException("folder title already exists for user");
        }
        if (folder.getId() == null) folder.setId(UUID.randomUUID());
        folder.setUser(user);
        return folderRepo.save(folder);
    }

    @Transactional
    public TaskFolder edit(TaskFolder folder) {
        if (folder == null || folder.getId() == null) throw new BusinessException("folder id required for edit");
        TaskFolder existing = folderRepo.findById(folder.getId()).orElseThrow(() -> new BusinessException("folder not found"));
        if (folder.getTitle() != null) existing.setTitle(folder.getTitle());
        if (folder.getNote() != null) existing.setNote(folder.getNote());
        return folderRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public TaskFolder get(UUID id) {
        return folderRepo.findById(id).orElseThrow(() -> new BusinessException("folder not found"));
    }

    @Transactional
    public void delete(UUID id) {
        if (!folderRepo.existsById(id)) throw new BusinessException("folder not found");
        folderRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskFolder> getByUser(UUID userId) {
        if (userId == null) throw new BusinessException("user id required");
        List<TaskFolder> list = folderRepo.findByUser_Id(userId);
        if (list == null) return Collections.emptyList();
        return list;
    }
}